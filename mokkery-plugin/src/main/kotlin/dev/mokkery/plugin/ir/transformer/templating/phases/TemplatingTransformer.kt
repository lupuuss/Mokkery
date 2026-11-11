package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedPrimaryConstructor
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.applyTransformChildrenVoid
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.hasNonDispatchParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.mokkeryFunctionId
import dev.mokkery.plugin.ir.transformer.core.irCallListGet
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.templating.matcher.isMatcher
import dev.mokkery.plugin.ir.transformer.templating.runMatcherPhases
import org.jetbrains.kotlin.builtins.StandardNames.BUILT_INS_PACKAGE_FQ_NAME
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
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.impl.IrSpreadElementImpl
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.isFinalClass
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.utils.memoryOptimizedMap

/**
 * Replaces calls on mocks with `runTemplate` calls that record the called function and its argument matchers;
 * plain arguments are wrapped with `eq`.
 *
 * Assumes the matcher phases ran. Matchers are recognised by their `ArgMatcher` type, because after
 * [MatcherIntrinsicsLoweringTransformer] the called function no longer identifies a matcher.
 * Mocked types can't use `ArgMatcher` in their members, so the type is unambiguous.
 */
class TemplatingTransformer(
    pluginScope: IrMokkeryPluginScope,
    private val templatingScopeParam: IrValueParameter,
) : CoreTransformer(pluginScope) {

    private val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    private val defaultValuesMatcherConstructor = referencedPrimaryConstructor(MokkeryIr.Class.DefaultValuesMatcher)
    private val runTemplateBlockingFun = referenced(MokkeryIr.Function.runTemplate)
    private val runTemplateSuspendFun = referenced(MokkeryIr.Function.runTemplateSuspend)
    private val eqFunction = runMatcherPhases(referenced(MokkeryIr.Function.eq))

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.type.isMatcher()) return expression
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
        return expression.replaceDeclarationIrBuilder {
            irBlock(resultType = runTemplateFun.returnType.substitute(substitution)) {
                val receiverVar = createTmpVariable(receiver)
                +irCall(runTemplateFun, runTemplateFun.returnType.substitute(substitution)) {
                    typeArguments[0] = expression.type
                    arguments[0] = irGet(templatingScopeParam)
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
                        expression.usesMatchers -> irNull()
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
        val hasDefaults = expression.arguments.any { it == null }
        val defaultsMatcherVar = when {
            hasDefaults -> createTmpVariable(irCallDefaultValuesMatcherContractorFor(expression))
            else -> null
        }
        +irReturn(
            irCallListOf(
                type = argMatcherClass.typeWith(irBuiltIns.anyNType),
                elements = calledFunc.nonDispatchParameters.memoryOptimizedMap {
                    val argument = expression
                        .arguments[it]
                        ?.wrapDispatchersWithMockCheck(referenced(MokkeryIr.Function.checkMockMemberCallResultAccess))
                        ?.patchDeclarationParents(lambda)
                    replaceTopTemplatingArg(argument, it, defaultsMatcherVar)
                }
            )
        )
    }

    private val IrCall.usesMatchers: Boolean
        get() = arguments.any { it?.type?.isMatcher() == true || (it is IrVararg && it.varargElementType.isMatcher()) }

    private fun IrBuilderWithScope.replaceTopTemplatingArg(
        arg: IrExpression?,
        param: IrValueParameter,
        defaultValueMatcherVar: IrVariable?,
    ): IrExpression = when {
        param.isVararg -> replaceVararg(arg)
        arg == null -> irGet(defaultValueMatcherVar!!)
        arg.type.isMatcher() -> arg
        else -> eq(arg)
    }

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

    private fun IrBuilderWithScope.replaceVararg(expression: IrExpression?): IrExpression {
        if (expression == null) return compositeVarargMatcher(emptyList())
        if (expression !is IrVararg) {
            error("Expected vararg expression but received ${expression.dumpKotlinLike()}!")
        }
        if (expression.varargElementType.type.isMatcher()) {
            return compositeVarargMatcher(expression.elements.map { it })
        }
        val singleArraySpread = expression.extractSingleArraySpreadOfMatchers()
        if (singleArraySpread != null) {
            return compositeVarargMatcher(singleArraySpread.elements.map { it })
        }
        return compositeVarargMatcher(
            expression.elements.map {
                when (it) {
                    is IrSpreadElement -> spreadLiteralsAsMatchers(it)
                    is IrExpression -> eq(it)
                    else -> error("Unexpected spread expression: $it")
                }
            }
        )
    }

    private fun IrBuilderWithScope.eq(value: IrExpression) = irCall(eqFunction, argMatcherClass.typeWith(irBuiltIns.anyNType)) {
        typeArguments[0] = irBuiltIns.anyNType
        arguments[0] = irGet(templatingScopeParam)
        arguments[1] = value
    }

    private fun IrBuilderWithScope.spreadLiteralsAsMatchers(spread: IrSpreadElement): IrSpreadElement {
        val array = spread.expression
        if (array.type.isNothing()) return spread
        val matchersArrayType = irBuiltIns.arrayClass.typeWith(argMatcherClass.typeWith(irBuiltIns.anyNType))
        return IrSpreadElementImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            expression = irCall(referenced(MokkeryIr.Function.internalSpreadArray), matchersArrayType) {
                typeArguments[0] = irBuiltIns.anyNType
                arguments[0] = eq(array)
            }
        )
    }

    private fun IrBuilderWithScope.compositeVarargMatcher(
        matchers: List<IrVarargElement>
    ) = irCallConstructor(referencedPrimaryConstructor(MokkeryIr.Class.CompositeVarArgMatcher)) {
        val matcherType = argMatcherClass.typeWith(irBuiltIns.anyNType)
        arguments[0] = irCall(referenced(MokkeryIr.Function.internalMaterialize), irBuiltIns.listClass.typeWith(matcherType)) {
            typeArguments[0] = irBuiltIns.anyNType
            arguments[0] = irVararg(matcherType, matchers)
        }
    }

    // detects (varargs = arrayOf(...))
    private fun IrVararg.extractSingleArraySpreadOfMatchers(): IrVararg? {
        if (elements.size != 1) return null
        val element = elements[0]
        if (element !is IrSpreadElement) return null
        val call = element.expression
        if (call !is IrCall) return null
        val vararg = call.arguments.singleOrNull { it is IrVararg && it.varargElementType.isMatcher() }
        if (vararg == null) return null
        val func = call.symbol.owner
        val funcName = func.name.asString()
        if (func.kotlinFqName.parent() != BUILT_INS_PACKAGE_FQ_NAME) return null
        if (funcName != "arrayOf" && !funcName.endsWith("ArrayOf")) return null
        return vararg as IrVararg
    }

    private fun IrBlockBodyBuilder.irCallDefaultValuesMatcherContractorFor(
        call: IrCall
    ) = irCallConstructor(defaultValuesMatcherConstructor) {
        arguments[0] = irLong(call.calculateDefaultsMask())
        arguments[1] = createDefaultsExtractingLambdaFor(call)
        arguments[2] = irBoolean(call.symbol.owner.isSuspend)
    }

    private fun IrCall.calculateDefaultsMask(): Long {
        var mask = 0L
        for (index in 1..<arguments.size) {
            if (arguments[index] == null) mask = mask or (1L shl (index - 1))
        }
        return mask
    }

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
            val substitutionMap = call.typeSubstitutionMap
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
