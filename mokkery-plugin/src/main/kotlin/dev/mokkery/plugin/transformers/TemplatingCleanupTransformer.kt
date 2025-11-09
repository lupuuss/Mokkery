package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.ir.getProperty
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.transformArguments
import org.jetbrains.kotlin.backend.common.lower.irImplicitCoercionToUnit
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetClass
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.typeWith

class TemplatingCleanupTransformer(
    compilerPluginScope: CompilerPluginScope,
    private val templatingFunctionSymbol: IrSimpleFunctionSymbol,
) : CoreTransformer(compilerPluginScope) {

    private val templatingResultClass = getClass(Mokkery.Class.TemplateOriginalResult)
    private val templatingResultClassSymbol = templatingResultClass.symbol
    private val valueGetter = templatingResultClass.getProperty("value").getter!!

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression) = expression.transformPostfix {
        transformArguments { it?.unwrapResultIfPossible() }
    }

    override fun visitSetValue(expression: IrSetValue) = expression.transformPostfix {
        value = value.unwrapResultIfPossible()
    }

    override fun visitSetField(expression: IrSetField) = expression.transformPostfix {
        value = value.unwrapResultIfPossible()
    }

    override fun visitVariable(declaration: IrVariable) = declaration.transformPostfix {
        initializer = initializer?.unwrapResultIfPossible()
    }

    override fun visitLoop(loop: IrLoop) = loop.transformPostfix {
        condition = condition.unwrapResultIfPossible()
    }

    override fun visitGetClass(expression: IrGetClass): IrExpression = expression.transformPostfix {
        argument = argument.unwrapResultIfPossible()
    }

    override fun visitWhen(expression: IrWhen) = expression.transformPostfix {
        if (branches.isEmpty()) return@transformPostfix
        var allTemplatingResult = true
        branches.forEach {
            if (!it.result.type.isTemplatingResult()) allTemplatingResult = false
            it.condition = it.condition.unwrapResultIfPossible()
        }
        if (allTemplatingResult) {
            type = type.toTemplatingResult()
            return@transformPostfix
        }
        branches.forEach { it.result = it.result.unwrapResultIfPossible() }
    }

    override fun visitTry(aTry: IrTry) = aTry.transformPostfix {
        if (tryResult.type.isTemplatingResult() && aTry.catches.all { it.result.type.isTemplatingResult() }) {
            type = type.toTemplatingResult()
            return@transformPostfix
        }
        tryResult = tryResult.unwrapResultIfPossible()
        catches.forEach { it.result = it.result.unwrapResultIfPossible() }
    }

    override fun visitReturn(expression: IrReturn) = expression.transformPostfix {
        if (expression.returnTargetSymbol == templatingFunctionSymbol) {
            val unitType = pluginContext.irBuiltIns.unitType
            expression.type = unitType
            expression.value = declarationIrBuilder { irImplicitCoercionToUnit(expression.value) }
            return@transformPostfix
        }
        if (expression.type.isTemplatingResult()) return@transformPostfix
        value = value.unwrapResultIfPossible()
    }

    override fun visitBlock(expression: IrBlock) = expression.transformPostfix {
        val lastExpression = statements.lastOrNull() as? IrExpression ?: return@transformPostfix
        if (!lastExpression.type.isTemplatingResult()) return@transformPostfix
        type = lastExpression.type
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock): IrExpression {
        if (expression.statements.isEmpty()) return expression
        val blockSymbol = expression.symbol
        val blockReturns = mutableListOf<IrReturn>()
        var anyNotTemplating = false
        expression.statements.forEach {
            if (it is IrReturn && it.returnTargetSymbol == blockSymbol) {
                val transformed = it.value.transform(this, null)
                it.value = transformed
                if (!transformed.type.isTemplatingResult()) anyNotTemplating = true
                blockReturns.add(it)
            } else {
                it.transformChildrenVoid()
            }
        }
        if (blockReturns.isEmpty()) {
            val lastExpression = expression.statements.lastOrNull() as? IrExpression ?: return expression
            if (lastExpression.type.isTemplatingResult()) expression.type = lastExpression.type
            return expression
        }
        if (anyNotTemplating) {
            blockReturns.forEach { it.value = it.value.unwrapResultIfPossible() }
            return expression
        }
        val switchedType = expression.type.toTemplatingResult()
        expression.type = switchedType
        blockReturns.forEach { it.type = switchedType }
        return expression
    }

    private fun IrExpression.unwrapResultIfPossible() = when (val arg = this) {
        is IrCall if arg.type.isTemplatingResult() -> declarationIrBuilder {
            irCall(valueGetter) { arguments[0] = arg }
        }
        else -> arg
    }

    private fun IrType.isTemplatingResult(): Boolean {
        return classOrNull == templatingResultClassSymbol
    }

    private fun IrType.toTemplatingResult(): IrType = templatingResultClass.typeWith(this)
}
