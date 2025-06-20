package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.inlineHereIgnoringReturns
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression

class ContextFunctionsInliner(compilerPluginScope: CompilerPluginScope) : CoreTransformer(compilerPluginScope) {

    private val extFunction = getFunction(Mokkery.Function.ext)

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner == extFunction) {
            super.visitCall(expression)
            return inlineExt(expression)
        }
        return super.visitCall(expression)
    }

    private fun inlineExt(call: IrCall): IrExpression {
        val calledFun = call.symbol.owner
        val blockParam = call.arguments[calledFun.parameters[2]] as IrFunctionExpression
        return declarationIrBuilder(call) {
            irBlock {
                inlineHereIgnoringReturns(blockParam.function, listOf(createTmpVariable(call.arguments[1]!!)))
            }
        }
    }
}
