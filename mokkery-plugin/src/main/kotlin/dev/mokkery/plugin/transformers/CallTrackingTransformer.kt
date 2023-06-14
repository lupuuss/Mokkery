package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.firstFunction
import dev.mokkery.plugin.ext.irVararg
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class CallTrackingTransformer(
    private val pluginContext: IrPluginContext,
    private val mockTable: Map<IrClass, IrClass>,
) : IrElementTransformerVoid() {

    private val irFunctions = IrFunctions()

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        return when (function.kotlinFqName) {
            Mokkery.Function.every -> transformEvery(expression, irFunctions.internalEvery)
            Mokkery.Function.verify -> transformVerify(expression, irFunctions.internalVerify)
            Mokkery.Function.everySuspend -> transformEvery(expression, irFunctions.internalEverySuspend)
            Mokkery.Function.verifySuspend -> transformVerify(expression, irFunctions.internalVerifySuspend)
            else -> super.visitCall(expression)
        }
    }

    private fun transformEvery(expression: IrCall, function: IrSimpleFunctionSymbol): IrCall {
        val nestedTransformer = CallTrackingNestedTransformer(mockTable)
        val block = expression.getValueArgument(0)!!
        block.transformChildren(nestedTransformer, null)
        return DeclarationIrBuilder(pluginContext, expression.symbol).run {
            irCall(function).apply {
                putValueArgument(0, irVararg(nestedTransformer.trackedExpressions.toList()))
                putValueArgument(1, block)
            }
        }
    }

    private fun transformVerify(expression: IrCall, function: IrSimpleFunctionSymbol): IrCall {
        val nestedTransformer = CallTrackingNestedTransformer(mockTable)
        val mode = expression.getValueArgument(0)
        val block = expression.getValueArgument(1)!!
        block.transformChildren(nestedTransformer, null)
        return DeclarationIrBuilder(pluginContext, expression.symbol).run {
            irCall(function).apply {
                putValueArgument(0, irVararg(nestedTransformer.trackedExpressions.toList()))
                putValueArgument(1, mode)
                putValueArgument(2, block)
            }
        }
    }

    inner class IrFunctions {
        val internalEvery = pluginContext.firstFunction(Mokkery.FunctionId.internalEvery)
        val internalEverySuspend = pluginContext.firstFunction(Mokkery.FunctionId.internalEverySuspend)
        val internalVerify = pluginContext.firstFunction(Mokkery.FunctionId.internalVerify)
        val internalVerifySuspend = pluginContext.firstFunction(Mokkery.FunctionId.internalVerifySuspend)
    }
}

