package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.argumentTypes
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.transformer.core.irCallEqMatcher
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrSpreadElementImpl
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.transformInPlace
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap

class MatchersInliningTransformer(
    pluginScope: IrMokkeryPluginScope,
    initialValueDeclarations: List<IrValueDeclaration>
) : CoreTransformer(pluginScope) {

    private val matcherValueDeclarations = initialValueDeclarations.toMutableSet()

    private val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    private val spreadArgMatcherFun = referenced(MokkeryIr.Function.spread)
    private val argMatchersScopeType = referencedDefaultType(MokkeryIr.Class.MokkeryMatcherScope)
    private val matchesFunction = referenced(MokkeryIr.Function.matches)
    private val materializeFunction = referenced(MokkeryIr.Function.materialize)
    private val materializeVarargFunction = referenced(MokkeryIr.Function.materializeVararg)
    private val inlineLiteralsAsMatchersFunc = referenced(MokkeryIr.Function.inlineLiteralsAsMatchers)
    private val contextFunctions = setOf(Mokkery.Name.ext, Mokkery.Name.ctx)
    private val boundValueKinds = setOf(IrParameterKind.ExtensionReceiver, IrParameterKind.Regular)
    private val blockParameterKinds = setOf(IrParameterKind.ExtensionReceiver, IrParameterKind.Context)

    fun transformDefaultValue(parameter: IrValueParameter) {
        val defaultValue = parameter.defaultValue ?: return
        defaultValue.expression = defaultValue.expression
            .transform(this, null)
            .performEqMatcherWrapping(parameter.type)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.kotlinFqName in contextFunctions) return expression.transformContextFunctionCall()
        val matcher = compileIfMatcher(expression.symbol.owner)
        expression.transformChildrenVoid()
        if (matcher.isCompiledMatcher != true) return expression
        return expression.replaceDeclarationIrBuilder { replaceMatcher(expression) }
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        val isPlainValue = declaration.type.isArgMatcherValueType()
                || declaration.initializer?.type?.isArgMatcherValueType() == true
        if (isPlainValue) return declaration.apply { transformChildrenVoid() }
        return transformMatcherVariable(declaration)
    }

    private fun transformMatcherVariable(declaration: IrVariable) = declaration.transformPostfix {
        val initializerType = initializer?.type ?: return@transformPostfix
        if (!initializerType.isMatcher() && !initializerType.isMatchersArray()) return@transformPostfix
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
                is IrSpreadElement -> element.toMatcherElement()
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
        if (lastExpression.type.isMatcher() || lastExpression.type.isMatchersArray()) {
            type = lastExpression.type
        }
    }

    override fun visitWhen(expression: IrWhen) = expression.transformPostfix {
        if (type.isArgMatcherValueType()) return@transformPostfix
        val matchersArrayType = branches.firstOrNull { it.result.type.isMatchersArray() }?.result?.type
        val arrayBranchesOnly = branches.all { it.result.type.isAnyArray() || it.result.type.isNothing() }
        val targetType = when {
            matchersArrayType != null && arrayBranchesOnly -> matchersArrayType
            matchersArrayType != null -> return@transformPostfix
            branches.any { it.result.type.isMatcher() } -> argMatcherClass.typeWith(type)
            else -> return@transformPostfix
        }
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

    private fun IrCall.transformContextFunctionCall(): IrExpression {
        val block = symbol.owner.parameters.last()
        val values = symbol.owner.parameters.filter { it.kind in boundValueKinds && it != block }
        values.forEach { arguments[it] = arguments[it]?.transform(this@MatchersInliningTransformer, null) }
        val lambda = (arguments[block] as? IrFunctionExpression)?.function
        val blockParameters = lambda?.parameters?.filter { it.kind in blockParameterKinds }.orEmpty()
        values.zip(blockParameters) { value, blockParameter ->
            val type = arguments[value]?.type
            if (type?.isMatcher() == true) {
                blockParameter.type = type
                matcherValueDeclarations += blockParameter
            }
        }
        arguments[block] = arguments[block]?.transform(this@MatchersInliningTransformer, null)
        return this
    }

    private fun IrBuilderWithScope.replaceMatcher(call: IrCall): IrExpression {
        val originalMatcherFunction = call.symbol.owner
        when (originalMatcherFunction) {
            matchesFunction -> return call.arguments[1]!!
            materializeFunction -> return call.arguments[1]!!.wrapAsMatcherOrError(call.type)
            materializeVarargFunction -> return replaceMaterializeVararg(call)
        }
        val matcherFunction = compileIfMatcher(originalMatcherFunction)
        val substitution = call.typeSubstitutionMap
        return irCall(matcherFunction) {
            call.typeArguments.forEachIndexed { i, it -> typeArguments[i] = it }
            matcherFunction
                .parameters
                .forEachIndexed { i, it ->
                    arguments[it] = replaceNestedTemplatingArg(call.arguments[i], it, substitution)
                }
        }
    }

    private fun IrBuilderWithScope.replaceMaterializeVararg(call: IrCall): IrExpression {
        val elementType = call.type.argumentTypes.single()!!
        val elements = (call.arguments[1] as? IrVararg)?.elements.orEmpty()
        return irCallListOf(type = elementType, elements = elements.toMatcherElements(elementType))
    }

    private fun IrExpression.wrapAsMatcherOrError(targetType: IrType): IrExpression = when {
        type.isMatchersArray() -> error("Vararg matcher passed without spread!")
        else -> performEqMatcherWrapping(targetType)
    }

    private fun List<IrVarargElement>.toMatcherElements(elementType: IrType): List<IrVarargElement> = map {
        when (it) {
            is IrSpreadElement -> it.toMatcherElement()
            is IrExpression -> it.wrapAsMatcherOrError(elementType)
            else -> it
        }
    }

    private fun IrSpreadElement.toMatcherElement(): IrVarargElement = when {
        expression.type.isMatcher() -> spreadArgMatcher(expression)
        expression.type.isMatchersArray() -> this
        else -> spreadLiteralsAsMatchers(this)
    }

    private fun IrBuilderWithScope.replaceNestedTemplatingArg(
        arg: IrExpression?,
        param: IrValueParameter,
        substitution: Map<IrTypeParameterSymbol, IrType>,
    ): IrExpression? = when {
        param.kind == IrParameterKind.DispatchReceiver || param.type == argMatchersScopeType -> arg
        arg is IrVararg && param.isMatchersVararg -> param.varargElementType!!
            .substitute(substitution)
            .let { irVararg(it, arg.elements.toMatcherElements(it)) }
        arg == null -> null
        param.type.isMatcher() -> arg.wrapAsMatcherOrError(param.type.substitute(substitution))
        else -> arg.performEqMatcherWrapping(param.type.substitute(substitution))
    }

    private fun IrVararg.usesMatchers() = elements.any {
        when (it) {
            is IrSpreadElement -> it.expression.type.isMatcher()
            is IrExpression -> it.type.isMatcher()
            else -> false
        }
    }

    private fun callEqMatcher(expression: IrExpression, targetType: IrType) = expression.replaceDeclarationIrBuilder {
        irCallEqMatcher(expression, targetType.argMatcherTypeArgumentOrNull() ?: expression.type)
    }

    private fun inlineLiteralsAsMatchers(
        array: IrExpression,
        targetType: IrType = inlineLiteralsAsMatchersFunc.returnType
    ) = array.replaceDeclarationIrBuilder {
        irCall(inlineLiteralsAsMatchersFunc, targetType) {
            arguments[0] = array
        }
    }

    private fun spreadLiteralsAsMatchers(spread: IrSpreadElement): IrSpreadElement = IrSpreadElementImpl(
        startOffset = spread.startOffset,
        endOffset = spread.endOffset,
        expression = inlineLiteralsAsMatchers(spread.expression)
    )

    private fun spreadArgMatcher(expression: IrExpression) = expression.replaceDeclarationIrBuilder {
        irCall(spreadArgMatcherFun) {
            typeArguments[0] = expression.type.argMatcherTypeArgumentOrNull() ?: irBuiltIns.anyNType
            arguments[0] = expression
        }
    }

    private fun IrType.argMatcherTypeArgumentOrNull() = argumentTypes.singleOrNull()

    private fun IrExpression.performEqMatcherWrapping(targetType: IrType): IrExpression {
        return when {
            targetType.isMatchersArray() && !type.isMatchersArray() -> inlineLiteralsAsMatchers(this, targetType)
            targetType.isMatcher() && !type.isMatcher() -> callEqMatcher(this, targetType)
            else -> this
        }
    }

    private val IrValueParameter.isMatchersVararg: Boolean
        get() = varargElementType?.isMatcher() == true
}
