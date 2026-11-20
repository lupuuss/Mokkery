package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.applyTransformChildrenVoid
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.transformer.core.irCallListGet
import dev.mokkery.plugin.ir.transformer.templating.irTemplate
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
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.isFinalClass
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
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
    private val runTemplateResultClass = referenced(MokkeryIr.Class.RunTemplateResult)
    private val varArgsFunction = referenced(MokkeryIr.Function.varArgs)
    private val defaultValueFunction = referenced(MokkeryIr.Function.defaultValue)
    private val materializeVarargFunction = referenced(MokkeryIr.Function.materializeVararg)
    private val checkMockMemberCallResultAccessFunction = referenced(MokkeryIr.Function.checkMockMemberCallResultAccess)
    private val checkMockFinalMemberCallFunction = referenced(MokkeryIr.Function.checkMockFinalMemberCall)

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.matcherFunctionKind() != null) return expression.wrapArgumentsWithMockCheck()
        val receiver = expression.dispatchReceiver
        val cls = receiver?.type?.getClass()
        if (receiver == null || cls?.isFinalClass == true) return expression.applyTransformChildrenVoid()
        if (expression.symbol.owner.modality == Modality.FINAL) {
            return expression
                .wrapDispatcherWithMockCheck()
                .applyTransformChildrenVoid()
        }
        if (receiver is IrCall) receiver.transformChildrenVoid()
        val originalCall = when {
            expression.usesMatchers -> null
            else -> expression
                .deepCopyWithSymbols()
                .templateArguments()
        }
        return expression.replaceDeclarationIrBuilder {
            irBlock(resultType = runTemplateResultClass.typeWith(expression.type)) {
                val receiverVar = createTmpVariable(receiver)
                +irTemplate(
                    templatingScope = matcherScope,
                    mock = irGet(receiverVar),
                    function = expression.symbol.owner,
                    resultType = expression.type,
                    matchers = { templatingArguments(it, expression) },
                    original = originalCall?.let { call ->
                        {
                            call.patchDeclarationParents(it)
                            call.arguments[0] = irGet(receiverVar)
                            call
                        }
                    },
                )
            }
        }
    }

    private fun IrBlockBodyBuilder.templatingArguments(lambda: IrSimpleFunction, expression: IrCall): IrExpression {
        val calledFunc = expression.symbol.owner
        val hasDefaults = calledFunc.parameters.any { expression.isOmittedDefault(it) }
        val defaultsExtractingVar = when {
            hasDefaults -> createTmpVariable(createDefaultsExtractingLambdaFor(expression))
            else -> null
        }
        val matchers = calledFunc.nonDispatchParameters.memoryOptimizedMap {
            val argument = expression
                .arguments[it]
                ?.wrapDispatchersWithMockCheck()
                ?.patchDeclarationParents(lambda)
            templatingArg(expression, argument, it, defaultsExtractingVar)
        }
        return irMaterialize(matchers)
    }

    private fun IrCall.wrapArgumentsWithMockCheck(): IrCall = apply {
        symbol.owner.nonDispatchParameters.forEach {
            arguments[it] = arguments[it]?.wrapDispatchersWithMockCheck()
        }
    }

    private fun IrCall.templateArguments(): IrCall = apply {
        symbol.owner.nonDispatchParameters.forEach {
            arguments[it] = arguments[it]?.transform(this@TemplatingTransformer, null)
        }
    }

    private val IrCall.usesMatchers: Boolean
        get() = arguments.any { it?.matcherArity() != null }

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
    private fun IrExpression.wrapDispatchersWithMockCheck(): IrExpression = transform(
        transformer = object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall) = expression.transformPostfix { wrapDispatcherWithMockCheck() }
        },
        data = null
    )

    private fun IrCall.wrapDispatcherWithMockCheck(): IrCall {
        val dispatcher = dispatchReceiver ?: return this
        if (dispatcher.type.getClass()?.isFinalClass == true) return this
        val name = symbol.owner.name.asString()
        val check = when (symbol.owner.modality) {
            Modality.FINAL -> checkMockFinalMemberCallFunction
            else -> checkMockMemberCallResultAccessFunction
        }
        dispatchReceiver = dispatcher.replaceDeclarationIrBuilder {
            irCall(func = check, type = dispatcher.type) {
                arguments[0] = irGet(matcherScope)
                arguments[1] = dispatcher
                arguments[2] = irString(name)
                typeArguments[0] = dispatcher.type
            }
        }
        return this
    }

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
