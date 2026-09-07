package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.requirePropertyOwner
import org.jetbrains.kotlin.backend.common.lower.irImplicitCoercionToUnit
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrContainerExpression
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.types.typeWith

class TemplatingCleanupTransformer(
    pluginScope: IrMokkeryPluginScope,
    private val templatingFunctionSymbol: IrSimpleFunctionSymbol,
) : CoreTransformer(pluginScope) {

    private val runTemplateResultClass = referenced(MokkeryIr.Class.RunTemplateResult)
    private val valueGetter = runTemplateResultClass.requirePropertyOwner("value").getter!!

    private var isUnwrappingEnabled = true

    override fun visitExpression(expression: IrExpression): IrExpression = expression.unwrapped {
        unwrapping(enabled = true) { expression.transformChildrenVoid() }
    }

    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement = unwrapping(enabled = true) {
        super.visitDeclaration(declaration)
    }

    override fun visitBody(body: IrBody): IrBody = unwrapping(enabled = true) { super.visitBody(body) }

    override fun visitElement(
        element: IrElement
    ): IrElement = unwrapping(enabled = true) {
        super.visitElement(element)
    }

    override fun visitBlockBody(body: IrBlockBody): IrBody {
        body.statements.transformWithUnwrapping(enabled = false)
        return body
    }

    override fun visitContainerExpression(
        expression: IrContainerExpression
    ): IrExpression = expression.unwrapped {
        statements.transformWithUnwrapping(enabled = false)
        val lastExpression = valueExpression
        if (lastExpression != null && lastExpression.type.isTemplatingResult()) {
            type = lastExpression.type
        }
    }

    override fun visitLoop(loop: IrLoop): IrExpression {
        loop.condition = loop.condition.transformWithUnwrapping(enabled = true)
        loop.body = loop.body?.transformWithUnwrapping(enabled = false)
        return loop
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
        val consumesArgument = expression.operator != IrTypeOperator.IMPLICIT_COERCION_TO_UNIT
        expression.argument = expression.argument.transformWithUnwrapping(enabled = consumesArgument)
        return expression
    }

    override fun visitWhen(expression: IrWhen): IrExpression = expression.unwrapped {
        branches.forEach { branch ->
            branch.condition = branch.condition.transformWithUnwrapping(enabled = true)
            branch.result = branch.result.transformWithUnwrapping(enabled = false)
        }
        if (!isExhaustive) return@unwrapped
        if (!propagateTemplatingResultFrom(branches.map { it.result })) {
            branches.forEach { it.result = it.result.unwrapResultIfPossible() }
        }
    }

    override fun visitTry(aTry: IrTry): IrExpression = aTry.unwrapped {
        tryResult = tryResult.transformWithUnwrapping(enabled = false)
        catches.forEach { it.result = it.result.transformWithUnwrapping(enabled = false) }
        finallyExpression = finallyExpression?.transformWithUnwrapping(enabled = false)
        val exits = catches.map { it.result } + tryResult
        if (!propagateTemplatingResultFrom(exits)) {
            tryResult = tryResult.unwrapResultIfPossible()
            catches.forEach { it.result = it.result.unwrapResultIfPossible() }
        }
    }

    override fun visitReturn(expression: IrReturn): IrExpression {
        if (expression.returnTargetSymbol == templatingFunctionSymbol) {
            expression.value = expression.value.transformWithUnwrapping(enabled = false)
            expression.type = irBuiltIns.nothingType
            expression.value = expression.value.replaceDeclarationIrBuilder {
                irImplicitCoercionToUnit(expression.value)
            }
            return expression
        }
        val isValueDiscarded = expression.returnTargetSymbol is IrReturnableBlockSymbol
        expression.value = expression.value.transformWithUnwrapping(enabled = !isValueDiscarded)
        return expression
    }

    override fun visitReturnableBlock(
        expression: IrReturnableBlock
    ): IrExpression = expression.unwrapped {
        statements.transformWithUnwrapping(enabled = false)
        val returns = collectReturns()
        val lastExpression = valueExpression
        val exits = returns.map { it.value } + listOfNotNull(lastExpression)
        if (!propagateTemplatingResultFrom(exits)) {
            returns.forEach { it.value = it.value.unwrapResultIfPossible() }
            if (lastExpression != null) {
                statements[statements.lastIndex] = lastExpression.unwrapResultIfPossible()
            }
        }
    }

    private fun MutableList<IrStatement>.transformWithUnwrapping(enabled: Boolean) {
        for (index in this.indices) {
            this[index] = this[index].transformWithUnwrapping(enabled = enabled) as IrStatement
        }
    }

    private fun IrExpression.transformWithUnwrapping(enabled: Boolean) = unwrapping(enabled) {
        transform(this@TemplatingCleanupTransformer, null)
    }

    private fun IrStatement.transformWithUnwrapping(enabled: Boolean) = unwrapping(enabled) {
        transform(this@TemplatingCleanupTransformer, null)
    }

    private inline fun <T : IrExpression> T.unwrapped(block: T.() -> Unit): IrExpression {
        val isValueConsumed = isUnwrappingEnabled
        block()
        return if (isValueConsumed) unwrapResultIfPossible() else this
    }

    private inline fun <T> unwrapping(enabled: Boolean, block: () -> T): T {
        val previous = isUnwrappingEnabled
        isUnwrappingEnabled = enabled
        try {
            return block()
        } finally {
            isUnwrappingEnabled = previous
        }
    }

    private fun IrExpression.propagateTemplatingResultFrom(exits: List<IrExpression>): Boolean {
        if (!exits.allProduceTemplatingResult()) return false
        type = type.toTemplatingResult()
        return true
    }

    private fun IrExpression.unwrapResultIfPossible(): IrExpression {
        val arg = this
        if (!arg.type.isTemplatingResult()) return arg
        val valueType = arg.type.templatingResultValueType()
        return arg.replaceDeclarationIrBuilder { irCall(valueGetter, valueType) { arguments[0] = arg } }
    }

    private fun List<IrExpression>.allProduceTemplatingResult(): Boolean {
        val values = filterNot { it.type.isNothing() || it.isNullConst() }
        return values.isNotEmpty() && values.all { it.type.isTemplatingResult() }
    }

    private fun IrExpression.isNullConst(): Boolean = this is IrConst && value == null

    private val IrWhen.isExhaustive: Boolean
        get() = branches.lastOrNull() is IrElseBranch

    private val IrContainerExpression.valueExpression: IrExpression?
        get() = statements.lastOrNull()?.takeIf { it !is IrReturn } as? IrExpression

    private fun IrType.isTemplatingResult(): Boolean {
        return classOrNull == runTemplateResultClass.symbol
    }

    private fun IrType.templatingResultValueType(): IrType {
        return (this as? IrSimpleType)?.arguments?.singleOrNull()?.typeOrNull ?: irBuiltIns.anyNType
    }

    private fun IrType.toTemplatingResult(): IrType = runTemplateResultClass.typeWith(this)
}
