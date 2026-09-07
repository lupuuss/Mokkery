package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.transformer.core.irGetMokkeryScopeFor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.isVararg

context(scope: TransformerScope)
fun IrCall.replaceVerifyNoMoreCalls(): IrExpression = this.replaceDeclarationIrBuilder {
    val originalCall = this@replaceVerifyNoMoreCalls
    val varargParam = symbol.owner.parameters.find { it.isVararg }
    irCall(referenced(MokkeryIr.Function.internalVerifyNoMoreCalls)) {
        arguments[0] = irGetMokkeryScopeFor(originalCall)
        arguments[1] = varargParam?.let(originalCall.arguments::get)
    }
}
