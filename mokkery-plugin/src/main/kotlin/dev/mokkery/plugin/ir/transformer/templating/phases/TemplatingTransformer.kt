package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.applyTransformChildrenVoid
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.hasNonDispatchParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.mokkeryFunctionId
import dev.mokkery.plugin.ir.transformer.core.irCallListGet
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherArity
import dev.mokkery.plugin.ir.transformer.templating.matcher.templatingSubstitutionMap
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irLong
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.impl.IrSpreadElementImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.isFinalClass
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.utils.memoryOptimizedMap

/**
 * Replaces calls on mocks with `runTemplate` calls that record the called function and its argument matchers.
 * The arguments are passed to vararg `materialize`, so the matcher phases that run next make them matchers
 * like in any other matcher position. A vararg parameter is passed with `varArgs` and a missing
 * argument with `defaultValue`.
 *
 * Runs on source IR, right after `ctx` and `ext` are inlined.
 */
class TemplatingTransformer(
    pluginScope: IrMokkeryPluginScope,
    private val matcherScope: IrValueParameter,
) : PhaseTransformer(pluginScope) {

    private val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    private val runTemplateBlockingFun = referenced(MokkeryIr.Function.runTemplate)
    private val runTemplateSuspendFun = referenced(MokkeryIr.Function.runTemplateSuspend)
    private val varArgsFunction = referenced(MokkeryIr.Function.varArgs)
    private val defaultValueFunction = referenced(MokkeryIr.Function.defaultValue)
    private val materializeVarargFunction = referenced(MokkeryIr.Function.materializeVararg)

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.matcherFunctionKind() != null) return expression
        val receiver = expression.dispatchReceiver
        val cls = receiver?.type?.getClass()
        if (receiver == null || cls?.isFinalClass == true) return expression.applyTransformChildrenVoid()
        if (expression.symbol.owner.modality == Modality.FINAL) {
            return expression.wrapDispatchersWithMockCheck(referenced(MokkeryIr.Function.checkMockFinalMemberCall))
        }
        if (receiver is IrCall) receiver.transformChildrenVoid()
        val functionToReplace = expression.symbol.owner
        val runTemplateFun = if (functionToReplace.isSuspend) runTemplateSuspendFun else runTemplateBlockingFun
        val substitution = mapOf(runTemplateFun.typeParameters[0].symbol to expression.type)
        val usesMatchers = expression.usesMatchers
        return expression.replaceDeclarationIrBuilder {
            irBlock(resultType = runTemplateFun.returnType.substitute(substitution)) {
                val receiverVar = createTmpVariable(receiver)
                +irCall(runTemplateFun, runTemplateFun.returnType.substitute(substitution)) {
                    typeArguments[0] = expression.type
                    arguments[0] = irGet(matcherScope)
                    arguments[1] = irGet(receiverVar)
                    arguments[2] = kClassReference(functionToReplace.parentAsClass.defaultTypeErased)
                    arguments[3] = irLong(functionToReplace.mokkeryFunctionId)
                    arguments[4] = irString(functionToReplace.name.asString())
                    arguments[5] = when {
                        !functionToReplace.hasNonDispatchParameters() -> irNull()
                        else -> irLambdaOf(runTemplateFun.parameters[5].type.makeNotNull()) {
                            createTemplatingArgumentsLambdaBody(it, expression)
                        }
                    }
                    arguments[6] = when {
                        usesMatchers -> irNull()
                        else -> irLambdaOf(runTemplateFun.parameters[6].type.makeNotNull().substitute(substitution)) {
                            val original = expression.deepCopyWithSymbols(initialParent = it)
                            original.arguments[0] = irGet(receiverVar)
                            +irReturn(original)
                        }
                    }
                }
            }
        }
    }

    private fun IrBlockBodyBuilder.createTemplatingArgumentsLambdaBody(lambda: IrSimpleFunction, expression: IrCall) {
        val calledFunc = expression.symbol.owner
        val hasDefaults = calledFunc.parameters.any { expression.isOmittedDefault(it) }
        val defaultsExtractingVar = when {
            hasDefaults -> createTmpVariable(createDefaultsExtractingLambdaFor(expression))
            else -> null
        }
        val matchers = calledFunc.nonDispatchParameters.memoryOptimizedMap {
            val argument = expression
                .arguments[it]
                ?.wrapDispatchersWithMockCheck(referenced(MokkeryIr.Function.checkMockMemberCallResultAccess))
                ?.patchDeclarationParents(lambda)
            templatingArg(expression, argument, it, defaultsExtractingVar)
        }
        +irReturn(irMaterialize(matchers))
    }

    private val IrCall.usesMatchers: Boolean
        get() = arguments.any { it != null && it.matcherArity() != null }

    private fun IrBuilderWithScope.templatingArg(
        call: IrCall,
        arg: IrExpression?,
        param: IrValueParameter,
        defaultsExtractingVar: IrVariable?,
    ): IrExpression {
        val type = param.type.substitute(call.templatingSubstitutionMap)
        return when {
            param.isVararg && !call.isOmittedDefault(param) -> irVarArgs(type, arg.varargElements())
            arg == null -> irDefaultValue(type, call, defaultsExtractingVar!!)
            else -> arg
        }
    }

    private fun IrExpression?.varargElements(): List<IrVarargElement> = when (this) {
        null -> emptyList()
        is IrVararg -> elements
        else -> listOf(IrSpreadElementImpl(startOffset, endOffset, this))
    }

    private fun IrBuilderWithScope.irVarArgs(
        type: IrType,
        elements: List<IrVarargElement>,
    ) = irCall(varArgsFunction, type) {
        typeArguments[0] = type
        arguments[0] = irGet(matcherScope)
        arguments[1] = irAnyVararg(elements)
    }

    private fun IrBuilderWithScope.irDefaultValue(
        type: IrType,
        call: IrCall,
        extractingVar: IrVariable,
    ) = irCall(defaultValueFunction, type) {
        typeArguments[0] = type
        arguments[0] = irGet(matcherScope)
        arguments[1] = irLong(call.calculateDefaultsMask())
        arguments[2] = irGet(extractingVar)
        arguments[3] = irBoolean(call.symbol.owner.isSuspend)
    }

    private fun IrBuilderWithScope.irMaterialize(elements: List<IrVarargElement>): IrCall {
        val listType = irBuiltIns.listClass.typeWith(argMatcherClass.typeWith(irBuiltIns.anyNType))
        return irCall(materializeVarargFunction, listType) {
            typeArguments[0] = irBuiltIns.anyNType
            arguments[0] = irGet(matcherScope)
            arguments[1] = irAnyVararg(elements)
        }
    }

    private fun IrBuilderWithScope.irAnyVararg(elements: List<IrVarargElement>) = IrVarargImpl(
        startOffset = startOffset,
        endOffset = endOffset,
        type = irBuiltIns.arrayClass.typeWith(irBuiltIns.anyNType),
        varargElementType = irBuiltIns.anyNType,
        elements = elements,
    )

    // the receiver is not necessarily a call - nested calls have to be found in any expression,
    // for instance in a string concatenation or in vararg elements
    private fun IrExpression.wrapDispatchersWithMockCheck(func: IrSimpleFunction): IrExpression = transform(
        transformer = object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall) = expression.transformPostfix {
                val dispatcher = dispatchReceiver ?: return@transformPostfix
                dispatchReceiver = dispatcher.replaceDeclarationIrBuilder {
                    irCall(
                        func = func,
                        type = dispatcher.type
                    ) {
                        arguments[0] = dispatcher
                        arguments[1] = irString(expression.symbol.owner.name.asString())
                        typeArguments[0] = dispatcher.type
                    }
                }
            }
        },
        data = null
    )

    private fun IrCall.calculateDefaultsMask(): Long {
        var mask = 0L
        for (index in 1..<arguments.size) {
            if (isOmittedDefault(symbol.owner.parameters[index])) mask = mask or (1L shl (index - 1))
        }
        return mask
    }

    private fun IrCall.isOmittedDefault(param: IrValueParameter): Boolean =
        arguments[param] == null && (!param.isVararg || param.hasDefaultValue())

    private fun IrBuilderWithScope.createDefaultsExtractingLambdaFor(call: IrCall): IrFunctionExpression {
        val calledFunc = call.symbol.owner
        val lambdaClass = if (calledFunc.isSuspend) {
            irBuiltIns.suspendFunctionN(2)
        } else {
            irBuiltIns.functionN(2)
        }
        val lambdaType = lambdaClass.typeWith(
            irBuiltIns.anyNType,
            irBuiltIns.listClass.typeWith(irBuiltIns.anyNType),
            irBuiltIns.nothingType
        )
        return irLambdaOf(lambdaType) { func ->
            val substitutionMap = call.templatingSubstitutionMap
            +irCall(calledFunc, calledFunc.returnType.substitute(substitutionMap)) {
                call.typeArguments.forEachIndexed { i, it -> typeArguments[i] = it }
                arguments[0] = irGet(func.parameters[0])
                for (index in 1..<call.arguments.size) {
                    val list = irGet(func.parameters[1])
                    val params = call.symbol.owner.parameters
                    val arg = call.arguments[index]
                    if (arg == null) {
                        arguments[index] = null
                    } else {
                        arguments[index] = irAs(
                            argument = irCallListGet(list, index - 1),
                            type = params[index].type.substitute(substitutionMap)
                        )
                    }
                }
            }
        }
    }
}
