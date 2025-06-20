package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.ir.getProperty
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.transformArguments
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.types.classOrNull

class TemplatingResultUnwrapper(compilerPluginScope: CompilerPluginScope) : CoreTransformer(compilerPluginScope) {

    private val templatingResultClass = getClass(Mokkery.Class.TemplateOriginalResult)
    private val templatingResultClassSymbol = templatingResultClass.symbol
    private val valueGetter = templatingResultClass.getProperty("value").getter!!

    override fun visitCall(expression: IrCall): IrExpression {
        val result = super.visitCall(expression) as IrCall
        result.transformArguments { it?.unwrapResultIfPossible() }
        return result
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        val variable = super.visitVariable(declaration) as IrVariable
        variable.initializer = variable.initializer?.unwrapResultIfPossible()
        return variable
    }

    override fun visitWhen(expression: IrWhen): IrExpression {
        val whenExpr = super.visitWhen(expression) as IrWhen
        whenExpr.branches.forEach {
            it.result = it.result.unwrapResultIfPossible()
        }
        return whenExpr
    }

    override fun visitReturn(expression: IrReturn): IrExpression {
        val returnExpr = super.visitReturn(expression) as IrReturn
        returnExpr.value = returnExpr.value.unwrapResultIfPossible()
        return returnExpr
    }

    private fun IrExpression.unwrapResultIfPossible() = when (val arg = this) {
        is IrCall if arg.type.classOrNull == templatingResultClassSymbol -> declarationIrBuilder(arg) {
            irCall(valueGetter) { arguments[0] = arg }
        }
        else -> arg
    }
}
