package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.referencedPrimaryConstructor
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrSpreadElementImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.transformInPlace

class MatchersInliningTransformer(
    pluginScope: IrMokkeryPluginScope,
    private val matchersCompiler: MatchersCompiler,
    initialValueDeclarations: List<IrValueDeclaration>
) : CoreTransformer(pluginScope) {

    private val matcherValueDeclarations = initialValueDeclarations.toMutableSet()

    private val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    private val spreadArgMatcherFun = referenced(MokkeryIr.Function.spread)
    private val argMatchersScopeType = referencedDefaultType(MokkeryIr.Class.MokkeryMatcherScope)
    private val argMatcherEqualsClass = referencedPrimaryConstructor(MokkeryIr.Class.ArgMatcherEquals)
    private val matchesFunction = referenced(MokkeryIr.Function.matches)
    private val matchesCompositeFunction = referenced(MokkeryIr.Function.matchesComposite)
    private val inlineLiteralsAsMatchersFunc = referenced(MokkeryIr.Function.inlineLiteralsAsMatchers)

    override fun visitCall(expression: IrCall): IrExpression {
        val matcher = matchersCompiler.compileIfMatcher(expression.symbol.owner)
        expression.transformChildrenVoid()
        if (matcher.isCompiledMatcher != true) return expression
        return expression.replaceDeclarationIrBuilder { replaceMatcher(expression) }
    }

    override fun visitVariable(declaration: IrVariable) = declaration.transformPostfix {
        if (initializer?.type?.isMatcher() != true) return@transformPostfix
        type = initializer!!.type
        matcherValueDeclarations += this
    }

    override fun visitVararg(expression: IrVararg) = expression.transformPostfix {
        if (!this.usesMatchers()) return@transformPostfix
        val elementType = argMatcherClass.typeWith(varargElementType)
        varargElementType = elementType
        type = irBuiltIns.arrayClass.owner.typeWith(elementType)
        elements.transformInPlace { element ->
            when (element) {
                is IrSpreadElement -> when {
                    element.expression.type.isMatcher() -> spreadArgMatcher(element.expression)
                    element.expression.type.isMatchersArray() -> element
                    else -> spreadLiteralsAsMatchers(element)
                }
                is IrExpression -> element.performEqMatcherWrapping(elementType)
                else -> element
            }

        }
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock) = expression.transformPostfix {
        val returns = collectReturns()
        if (returns.none { it.value.type.isMatcher() }) return@transformPostfix
        val targetType = argMatcherClass.typeWith(type)
        type = targetType
        returns.forEach {
            it.type = targetType
            it.value = it.value.performEqMatcherWrapping(targetType)
        }
    }

    override fun visitBlock(expression: IrBlock) = expression.transformPostfix {
        val lastExpression = statements.lastOrNull() as? IrExpression ?: return@transformPostfix
        if (lastExpression.type.isMatcher()) {
            type = lastExpression.type
        }
    }

    override fun visitWhen(expression: IrWhen) = expression.transformPostfix {
        if (branches.none { branch -> branch.result.type.isMatcher() }) return@transformPostfix
        val targetType = argMatcherClass.typeWith(type)
        type = targetType
        branches.forEach { it.result = it.result.performEqMatcherWrapping(targetType) }
    }

    override fun visitGetValue(expression: IrGetValue) = expression.transformPostfix {
        if (symbol.owner !in matcherValueDeclarations) return@transformPostfix
        type = symbol.owner.type
    }

    override fun visitSetValue(expression: IrSetValue) = expression.transformPostfix {
        if (symbol.owner !in matcherValueDeclarations) return@transformPostfix
        value = value.performEqMatcherWrapping(symbol.owner.type)
    }

    override fun visitReturn(expression: IrReturn) = expression.transformPostfix {
        // equality matchers wrapping is performed only for functions
        // constructor cannot be a matcher
        // for returnable blocks, we will make decision when visiting it
        val func = returnTargetSymbol.owner as? IrSimpleFunction ?: return@transformPostfix
        value = value.performEqMatcherWrapping(func.returnType)
        type = func.returnType
    }

    private fun IrBuilderWithScope.replaceMatcher(expression: IrExpression): IrExpression {
        val call = expression as IrCall
        val originalMatcherFunction = call.symbol.owner
        if (originalMatcherFunction == matchesFunction) return expression.arguments[1]!!
        if (originalMatcherFunction == matchesCompositeFunction) return replaceMatchesComposite(expression)
        val matcherFunction = matchersCompiler.compileIfMatcher(originalMatcherFunction)
        return irCall(matcherFunction) {
            call.typeArguments.forEachIndexed { i, it -> typeArguments[i] = it }
            matcherFunction
                .parameters
                .forEachIndexed { i, it ->
                    arguments[it] = replaceNestedTemplatingArg(expression.arguments[i], it)
                }
        }
    }

    private fun IrBuilderWithScope.replaceMatchesComposite(expression: IrCall): IrExpression {
        val matchersVararg = expression.arguments[1] as IrVararg
        val builderBlock = expression.arguments[2]!!
        // extracting lambda return type
        val returnType = (builderBlock.type as IrSimpleType).arguments.last() as IrType
        return irBlock(resultType = returnType) {
            val elementType = argMatcherClass.typeWith(matchersVararg.varargElementType)
            val matchersInlined = createTmpVariable(
                irCallListOf(
                    type = elementType,
                    elements = replaceCompositeVararg(matchersVararg, elementType).elements
                )
            )
            +irInvoke(builderBlock, false, irGet(matchersInlined))
        }
    }

    private fun IrBuilderWithScope.replaceNestedTemplatingArg(
        arg: IrExpression?,
        param: IrValueParameter,
    ): IrExpression? = when {
        param.kind == IrParameterKind.DispatchReceiver || param.type == argMatchersScopeType -> arg
        arg is IrVararg && param.isMatchersVararg -> replaceCompositeVararg(arg, param.varargElementType!!)
        arg == null -> null
        else -> arg.performEqMatcherWrapping(param.type)
    }

    private fun IrVararg.usesMatchers() = elements.any {
        when (it) {
            is IrSpreadElement -> it.expression.type.isMatcher()
            is IrExpression -> it.type.isMatcher()
            else -> false
        }
    }

    private fun IrBuilderWithScope.replaceCompositeVararg(
        frontArg: IrVararg,
        varargElementType: IrType
    ): IrVararg {
        return irVararg(
            varargElementType,
            frontArg.elements.map { element ->
                when (element) {
                    is IrExpression -> element.performEqMatcherWrapping(varargElementType)
                    else -> element
                }
            }
        )
    }

    private fun callEqMatcher(expression: IrExpression) = expression.replaceDeclarationIrBuilder {
        irCallConstructor(argMatcherEqualsClass) {
            arguments[0] = expression
        }
    }

    private fun spreadLiteralsAsMatchers(spread: IrSpreadElement): IrSpreadElement = IrSpreadElementImpl(
        startOffset = spread.startOffset,
        endOffset = spread.endOffset,
        expression = spread.expression.replaceDeclarationIrBuilder {
            irCall(inlineLiteralsAsMatchersFunc) {
                arguments[0] = spread.expression
            }
        }
    )

    private fun spreadArgMatcher(expression: IrExpression) = expression.replaceDeclarationIrBuilder {
        irCall(spreadArgMatcherFun) {
            arguments[0] = expression
        }
    }

    private fun IrType.isMatcher(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == argMatcherClass) return true
        return clazz.isSubclassOf(argMatcherClass)
    }

    private fun IrType.isMatchersArray(): Boolean {
        return isArray() && this.getArrayElementType(irBuiltIns).isMatcher()
    }

    private fun IrExpression.performEqMatcherWrapping(targetType: IrType): IrExpression {
        return when {
            !targetType.isMatcher() -> this
            !this.type.isMatcher() -> callEqMatcher(this)
            else -> this
        }
    }

    private val IrValueParameter.isMatchersVararg: Boolean
        get() = varargElementType?.isMatcher() == true
}
