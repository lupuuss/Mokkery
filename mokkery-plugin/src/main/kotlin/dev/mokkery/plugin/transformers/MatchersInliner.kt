package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irVararg
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
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
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.nestedClasses
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.transformInPlace

class MatchersInliner(
    compilerPluginScope: CompilerPluginScope,
    private val compileIfMatcher: (IrSimpleFunction) -> IrSimpleFunction,
    initialValueDeclarations: List<IrValueDeclaration>
) : CoreTransformer(compilerPluginScope) {

    private val argMatcherClass = getClass(Mokkery.Class.ArgMatcher)
    private val varargMatcherClass = getClass(Mokkery.Class.VarArgMatcher)
    private val varargMatcherMarkerClass = getClass(Mokkery.Class.VarargMatcherMarker)
    private val argMatcherCompositeClass = argMatcherClass.nestedClasses.single { it.name.asString() == "Composite" }
    private val eqMatcher = argMatcherClass.nestedClasses
        .single { it.name.asString() == "Equals" }
        .primaryConstructor!!
    private val argMatchersScopeClass = getClass(Mokkery.Class.MokkeryMatcherScope)
    private val argMatchersScopeType = argMatchersScopeClass.defaultType
    private val matchesFunction = getFunction(Mokkery.Function.matches)
    private val matchesCompositeFunction = getFunction(Mokkery.Function.matchesComposite)
    private val matcherValueDeclarations = initialValueDeclarations.toMutableSet()
    private val inlineLiteralsAsMatchersFunc = getFunction(Mokkery.Function.inlineLiteralsAsMatchers)

    override fun visitCall(expression: IrCall): IrExpression {
        val matcher = compileIfMatcher(expression.symbol.owner)
        if (matcher.isCompiledMatcher != true) return super.visitCall(expression)
        super.visitCall(expression)
        return declarationIrBuilder {
            replaceMatcher(expression)
        }
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        val irVariable = super.visitVariable(declaration)
        if (irVariable !is IrVariable) return irVariable
        if (irVariable.initializer?.type?.isMatcher() != true) return irVariable
        irVariable.type = irVariable.initializer!!.type
        matcherValueDeclarations += irVariable
        return irVariable
    }

    override fun visitVararg(expression: IrVararg): IrExpression {
        val vararg = super.visitVararg(expression)
        if (vararg !is IrVararg) return vararg
        val usesMatchers = vararg.elements.any {
            when (it) {
                is IrSpreadElement -> it.expression.type.isMatcher()
                is IrExpression -> it.type.isMatcher()
                else -> false
            }
        }
        if (!usesMatchers) return vararg
        val elementType = argMatcherClass.typeWith(vararg.varargElementType)
        vararg.varargElementType = elementType
        vararg.type = pluginContext.irBuiltIns.arrayClass.owner.typeWith(elementType)
        vararg.elements.transformInPlace { element ->
            when (element) {
                is IrSpreadElement -> when {
                    element.expression.type.isMatcher() -> element.expression
                    element.expression.type.isMatchersArray() -> element
                    else -> spreadLiteralsAsMatchers(element)
                }
                is IrExpression -> element.performEqMatcherWrapping(elementType)
                else -> element
            }

        }
        return vararg
    }

    override fun visitWhen(expression: IrWhen): IrExpression {
        val irWhen = super.visitWhen(expression)
        if (irWhen !is IrWhen) return irWhen
        if (expression.branches.none { branch -> branch.result.type.isMatcher() }) return irWhen
        val anyVarargMatchers = expression.branches.any { it.result.type.isVarArgMatcher() }
        val targetType = when {
            anyVarargMatchers -> varargMatcherClass.defaultType
            else -> argMatcherClass.typeWith(irWhen.type)
        }
        irWhen.type = targetType
        irWhen.branches.forEach { it.result = it.result.performEqMatcherWrapping(targetType) }
        return irWhen
    }

    override fun visitGetValue(expression: IrGetValue): IrExpression {
        val irGet = super.visitGetValue(expression)
        if (irGet !is IrGetValue) return irGet
        if (irGet.symbol.owner !in matcherValueDeclarations) return irGet
        irGet.type = irGet.symbol.owner.type
        return irGet
    }

    override fun visitSetValue(expression: IrSetValue): IrExpression {
        val irSet = super.visitSetValue(expression)
        if (irSet !is IrSetValue) return irSet
        if (irSet.symbol.owner !in matcherValueDeclarations) return irSet
        irSet.value = irSet.value.performEqMatcherWrapping(irSet.symbol.owner.type)
        return irSet
    }

    override fun visitReturn(expression: IrReturn): IrExpression {
        val returnExpression = super.visitReturn(expression)
        if (returnExpression !is IrReturn) return returnExpression
        val func = returnExpression.returnTargetSymbol.owner as? IrSimpleFunction ?: return returnExpression
        returnExpression.value = returnExpression.value.performEqMatcherWrapping(targetType = func.returnType)
        returnExpression.type = func.returnType
        return returnExpression
    }

    private fun IrBuilderWithScope.replaceMatcher(expression: IrExpression): IrExpression {
        val call = expression as IrCall
        val originalMatcherFunction = call.symbol.owner
        if (originalMatcherFunction == matchesFunction) return expression.arguments[1]!!
        if (originalMatcherFunction == matchesCompositeFunction) return replaceMatchesComposite(expression)
        val matcherFunction = compileIfMatcher(originalMatcherFunction)
        var matcherTargetType = matcherFunction.returnType
        return irCall(matcherFunction) {
            call.typeArguments.forEachIndexed { i, it -> typeArguments[i] = it }
            matcherFunction
                .parameters
                .forEachIndexed { i, it ->
                    val arg = replaceNestedTemplatingArg(expression.arguments[i], it)
                    arguments[it] = arg
                    if (arg?.type?.isVarArgMatcher() == true) {
                        matcherTargetType = varargMatcherClass.defaultType
                    }
                }
        }.performVarArgMarkerWrapping(matcherTargetType)
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
                    transformerScope = this@MatchersInliner,
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

    private fun callEqMatcher(expression: IrExpression) = declarationIrBuilder {
        irCallConstructor(eqMatcher) {
            arguments[0] = expression
        }
    }

    private fun spreadLiteralsAsMatchers(spread: IrSpreadElement): IrSpreadElement {
        return declarationIrBuilder {
            IrSpreadElementImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                expression = irCall(inlineLiteralsAsMatchersFunc) {
                    arguments[0] = spread.expression
                }
            )
        }
    }

    private fun varargMatcherMarker(expression: IrExpression): IrExpression {
        val constructor = varargMatcherMarkerClass.primaryConstructor!!
        return declarationIrBuilder {
            irCallConstructor(constructor) {
                arguments[0] = expression
            }
        }
    }

    private fun IrType.isMatcher(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == argMatcherClass) return true
        return clazz.isSubclassOf(argMatcherClass)
    }

    private fun IrType.isMatchersArray(): Boolean {
        return isArray() && this.getArrayElementType(pluginContext.irBuiltIns).isMatcher()
    }

    private fun IrType.isComposite(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == argMatcherCompositeClass) return true
        return clazz.isSubclassOf(argMatcherCompositeClass)
    }

    private fun IrType.isVarArgMatcher(): Boolean {
        val classSymbol = classOrNull ?: return false
        val clazz = classSymbol.owner
        if (clazz == varargMatcherClass) return true
        return clazz.isSubclassOf(varargMatcherClass)
    }

    private fun IrExpression.performEqMatcherWrapping(targetType: IrType): IrExpression {
        return when {
            !targetType.isMatcher() -> this
            !this.type.isMatcher() -> callEqMatcher(this)
            else -> this
        }
    }

    private fun IrExpression.performVarArgMarkerWrapping(targetType: IrType): IrExpression {
        return when {
            this.type.isComposite() && targetType.isVarArgMatcher() -> varargMatcherMarker(this)
            else -> this
        }
    }

    private val IrValueParameter.isMatchersVararg: Boolean
        get() = varargElementType?.isMatcher() == true
}
