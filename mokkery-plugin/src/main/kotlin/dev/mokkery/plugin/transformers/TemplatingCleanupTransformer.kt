package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.getProperty
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.transformArguments
import org.jetbrains.kotlin.backend.common.lower.irImplicitCoercionToUnit
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetClass
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.typeWith

class TemplatingCleanupTransformer(
    compilerPluginScope: CompilerPluginScope,
    private val templatingFunctionSymbol: IrSimpleFunctionSymbol,
) : CoreTransformer(compilerPluginScope) {

    private val runTemplateResultClass = getClass(Mokkery.Class.RunTemplateResult)
    private val runTemplateResultClassSymbol = runTemplateResultClass.symbol
    private val valueGetter = runTemplateResultClass.getProperty("value").getter!!

    override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) = expression.transformPostfix {
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

    override fun visitReturn(expression: IrReturn): IrExpression {
        if (expression.returnTargetSymbol == templatingFunctionSymbol) {
            return expression.transformPostfix {
                val unitType = pluginContext.irBuiltIns.unitType
                type = unitType
                value = declarationIrBuilder { irImplicitCoercionToUnit(value) }
            }
        }
        // while visiting IrReturn we cannot make a decision if returned value should be unwrapped or not
        // we make a decision while visiting IrReturnableBlock
        if (expression.returnTargetSymbol is IrReturnableBlockSymbol) {
            expression.value = expression.value.transform(this, null)
            return expression
        }
        return expression.transformPostfix {
            if (type.isTemplatingResult()) return@transformPostfix
            value = value.unwrapResultIfPossible()
        }
    }

    override fun visitBlock(expression: IrBlock) = expression.transformPostfix {
        val lastExpression = statements.lastOrNull() as? IrExpression ?: return@transformPostfix
        if (!lastExpression.type.isTemplatingResult()) return@transformPostfix
        type = lastExpression.type
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock) = expression.transformPostfix {
        if (type.isTemplatingResult()) return@transformPostfix
        val returns = collectReturns()
        when {
            returns.isEmpty() && type.isUnit() -> return@transformPostfix
            returns.isEmpty() -> {
                val lastExpr = statements.lastOrNull() as? IrExpression ?: return@transformPostfix
                if (lastExpr.type.isTemplatingResult()) type = lastExpr.type
                return@transformPostfix
            }
            returns.all { it.value.type.isTemplatingResult() } -> {
                type = type.toTemplatingResult()
                returns.forEach { it.type = it.value.type }
                return@transformPostfix
            }
            else -> returns.forEach { it.value = it.value.unwrapResultIfPossible() }
        }
    }

    private fun IrExpression.unwrapResultIfPossible() = when (val arg = this) {
        is IrCall if arg.type.isTemplatingResult() -> declarationIrBuilder {
            irCall(valueGetter) { arguments[0] = arg }
        }
        else -> arg
    }

    private fun IrType.isTemplatingResult(): Boolean {
        return classOrNull == runTemplateResultClassSymbol
    }

    private fun IrType.toTemplatingResult(): IrType = runTemplateResultClass.typeWith(this)
}
