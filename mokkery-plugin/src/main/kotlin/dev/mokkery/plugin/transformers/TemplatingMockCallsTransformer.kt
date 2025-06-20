package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.mokkeryErrorAt
import dev.mokkery.plugin.ir.asTypeParamOrNull
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irCallMapOf
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import org.jetbrains.kotlin.builtins.StandardNames.BUILT_INS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irLong
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.impl.IrSpreadElementImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isFinalClass
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap
import org.jetbrains.kotlin.utils.memoryOptimizedMap

class TemplatingMockCallsTransformer(
    compilerPluginScope: CompilerPluginScope,
    private val templatingScopeParam: IrValueParameter,
) : CoreTransformer(compilerPluginScope) {

    private val runTemplateBlockingFun = getFunction(Mokkery.Function.runTemplate)
    private val runTemplateSuspendFun = getFunction(Mokkery.Function.runTemplateSuspend)
    private val templatingParameterClass = getClass(Mokkery.Class.TemplatingParameter)
    private val templatingParameterConstructor = templatingParameterClass.primaryConstructor!!
    private val argMatcherClass = getClass(Mokkery.Class.ArgMatcher)
    private val expectDefaultMatcherConstructor = getClass(Mokkery.Class.DefaultValueMatcher).primaryConstructor!!
    private val eqMatcher = argMatcherClass.nestedClasses
        .single { it.name.asString() == "Equals" }
        .primaryConstructor!!
    private val inlineLiteralsAsMatchersFunc = getFunction(Mokkery.Function.inlineLiteralsAsMatchers)

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.type.isMatcher()) return expression
        val receiver = expression.dispatchReceiver
        val cls = receiver?.type?.getClass()
        if (receiver == null || cls?.isFinalClass == true) return super.visitCall(expression)
        if (receiver is IrCall) super.visitCall(receiver)
        val functionToReplace = expression.symbol.owner
        val runTemplateFun = if (functionToReplace.isSuspend) runTemplateSuspendFun else runTemplateBlockingFun
        return declarationIrBuilder {
            irCall(runTemplateFun) {
                typeArguments[0] = expression.type
                arguments[0] = irGet(templatingScopeParam)
                arguments[1] = expression.arguments[0]
                arguments[2] = kClassReference(functionToReplace.parentAsClass.defaultTypeErased)
                arguments[3] = irString(functionToReplace.name.asString())
                arguments[4] = irLambdaOf(runTemplateFun.parameters[4].type.makeNotNull()) {
                    createTemplatingLambdaBody(expression)
                }
                arguments[5] = if (expression.usesMatchers) {
                    irNull()
                } else {
                    irLambdaOf(runTemplateFun.parameters[5].type.makeNotNull()) {
                        +irReturn(expression.deepCopyWithSymbols(initialParent = it))
                    }
                }
            }
        }
    }

    private fun IrBlockBodyBuilder.createTemplatingLambdaBody(expression: IrCall) {
        val calledFunc = expression.symbol.owner
        val hasDefaults = expression.arguments.any { it == null }
        val defaults: DefaultsCallSpec? = if (hasDefaults) createDefaultsCallSpec(expression) else null
        +irReturn(
            irCallMapOf(
                transformer = this@TemplatingMockCallsTransformer,
                pairs = calledFunc.nonDispatchParameters.memoryOptimizedMap {
                    val param = irCallConstructor(templatingParameterConstructor) {
                        arguments[0] = irString(it.name.asString())
                        arguments[1] = irBoolean(it.isVararg)
                        val typeParam = it.type.asTypeParamOrNull()
                        if (typeParam in expression.arguments[0]!!.type.classOrFail.owner.typeParameters) {
                            arguments[3] = irInt(typeParam!!.index)
                        } else {
                            arguments[2] = kClassReference(it.type.eraseTypeParameters())
                        }
                    }
                    param to replaceTopTemplatingArg(expression.arguments[it], it, defaults)
                },
                keyType = templatingParameterClass.defaultType,
                valueType = argMatcherClass.typeWith(context.irBuiltIns.anyNType)
            )
        )
    }

    private val IrCall.usesMatchers: Boolean
        get() = arguments.any { it?.type?.isMatcher() == true || (it is IrVararg && it.varargElementType.isMatcher()) }

    private fun IrBuilderWithScope.replaceTopTemplatingArg(
        arg: IrExpression?,
        param: IrValueParameter,
        defaults: DefaultsCallSpec?,
    ): IrExpression = when {
        param.isVararg -> replaceVararg(arg)
        arg == null -> irCallConstructor(expectDefaultMatcherConstructor) {
            arguments[0] = irLong(defaults!!.mask)
            arguments[1] = defaults.callLambda
            arguments[2] = irBoolean(defaults.isSuspend)
        }
        arg.type.isMatcher() -> arg
        else -> callEqMatcher(arg)
    }

    private fun IrBuilderWithScope.replaceVararg(expression: IrExpression?): IrExpression {
        if (expression == null) return compositeVarargMatcher(emptyList())
        if (expression !is IrVararg) {
            mokkeryErrorAt(expression, "Expected vararg expression but received ${expression.dumpKotlinLike()}!")
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
                    else -> callEqMatcher(it as IrExpression)
                }
            }
        )
    }

    private fun IrBuilderWithScope.spreadLiteralsAsMatchers(spread: IrSpreadElement): IrSpreadElement {
        return IrSpreadElementImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            expression = irCall(inlineLiteralsAsMatchersFunc) {
                arguments[0] = spread.expression
            }
        )
    }

    private fun IrBuilderWithScope.compositeVarargMatcher(
        matchers: List<IrVarargElement>
    ) = irCallConstructor(getClass(Mokkery.Class.CompositeVarArgMatcher).primaryConstructor!!) {
        arguments[0] = irCallListOf(
            transformerScope = this@TemplatingMockCallsTransformer,
            type = argMatcherClass.typeWith(pluginContext.irBuiltIns.anyNType),
            elements = matchers
        )
    }

    private fun IrBuilderWithScope.callEqMatcher(expression: IrExpression) = irCallConstructor(eqMatcher) {
        arguments[0] = expression
    }

    private fun IrType.isMatcher(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == argMatcherClass) return true
        return clazz.isSubclassOf(argMatcherClass)
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

    private fun IrBlockBodyBuilder.createDefaultsCallSpec(expression: IrCall): DefaultsCallSpec {
        val builtIns = pluginContext.irBuiltIns
        val calledFunc = expression.symbol.owner
        val mask: Long = expression.arguments.foldIndexed(0L) { index, acc, value ->
            if (value == null) {
                acc or (1L shl index)
            } else {
                acc
            }
        }
        val lambdaClass = if (calledFunc.isSuspend) {
            builtIns.suspendFunctionN(2)
        } else {
            builtIns.functionN(2)
        }
        val lambdaType = lambdaClass.typeWith(
            builtIns.anyNType,
            builtIns.listClass.typeWith(builtIns.anyNType),
            builtIns.nothingType
        )
        val lambda = irLambdaOf(lambdaType) { func ->
            val substitutionMap = expression.typeSubstitutionMap
            +irCall(calledFunc) {
                arguments[0] = irGet(func.parameters[0])
                val list = irGet(func.parameters[1])
                val getFunc = list.type.classOrFail.getSimpleFunction("get")!!.owner
                for (index in 1..<expression.arguments.size) {
                    val params = expression.symbol.owner.parameters
                    val arg = expression.arguments[index]
                    if (arg == null) {
                        arguments[index] = null
                    } else {
                        arguments[index] = irAs(
                            argument = irCall(getFunc) {
                                arguments[0] = list
                                arguments[1] = irInt(index - 1)
                            },
                            type = params[index].type.substitute(substitutionMap)
                        )
                    }
                }
            }
        }
        val lambdaVar = createTmpVariable(lambda)
        return DefaultsCallSpec(mask, irGet(lambdaVar), calledFunc.isSuspend)
    }
}

private class DefaultsCallSpec(val mask: Long, val callLambda: IrExpression, val isSuspend: Boolean)
