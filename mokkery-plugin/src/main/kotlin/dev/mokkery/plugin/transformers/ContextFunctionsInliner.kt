package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.CompilerPluginScope
import dev.mokkery.plugin.core.CoreTransformer
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.ir.removeReturnsTargeting
import org.jetbrains.kotlin.backend.common.lower.VariableRemapper
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.statements

class ContextFunctionsInliner(compilerPluginScope: CompilerPluginScope) : CoreTransformer(compilerPluginScope) {

    private val contextFunctions = setOf(Mokkery.Name.ext, Mokkery.Name.ctx)

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol.owner.kotlinFqName in contextFunctions) {
            super.visitCall(expression)
            return inlineContextFunction(expression)
        }
        return super.visitCall(expression)
    }

    private fun inlineContextFunction(call: IrCall): IrExpression {
        val blockParam = call.arguments.last() as IrFunctionExpression
        return declarationIrBuilder {
            irBlock(resultType = pluginContext.irBuiltIns.unitType) {
                val variables = call.arguments.drop(1).dropLast(1).map { createTmpVariable(it!!) }
                val variablesRemapper = VariableRemapper(blockParam.function.parameters.zip(variables).toMap())
                blockParam.function
                    .body!!
                    .transform(variablesRemapper, null)
                    .removeReturnsTargeting(blockParam.function.symbol)
                    .statements
                    .forEach { +it }
            }
        }
    }
}
