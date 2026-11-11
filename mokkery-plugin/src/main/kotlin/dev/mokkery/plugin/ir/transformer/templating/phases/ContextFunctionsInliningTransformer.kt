package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.ir.inline
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.utils.memoryOptimizedMap

/**
 * Inlines `ctx` and `ext` calls: their values become local variables and the block body is inlined in place.
 *
 * Runs first, so the later phases see block parameters as ordinary variables.
 * Assumes the block is a lambda (enforced by FIR).
 */
class ContextFunctionsInliningTransformer(pluginScope: IrMokkeryPluginScope) : CoreTransformer(pluginScope) {

    override fun visitCall(expression: IrCall): IrExpression {
        expression.transformChildrenVoid()
        if (expression.symbol.owner.kotlinFqName !in contextFunctions) return expression
        val arguments = expression.arguments
        val block = arguments.last() as IrFunctionExpression
        return expression.replaceDeclarationIrBuilder {
            irBlock {
                val variables = arguments
                    .subList(1, arguments.lastIndex)
                    .memoryOptimizedMap { createTmpVariable(it!!) }
                +block.function.inline(parent, variables).withExplicitUnitReturn()
            }
        }
    }

    private fun IrReturnableBlock.withExplicitUnitReturn(): IrReturnableBlock = apply {
        if (!type.isUnit() || statements.lastOrNull() is IrReturn) return@apply
        statements += IrReturnImpl(
            startOffset = endOffset,
            endOffset = endOffset,
            type = irBuiltIns.nothingType,
            returnTargetSymbol = symbol,
            value = IrGetObjectValueImpl(endOffset, endOffset, irBuiltIns.unitType, irBuiltIns.unitClass),
        )
    }
}

private val contextFunctions = setOf(Mokkery.Name.ext, Mokkery.Name.ctx)
