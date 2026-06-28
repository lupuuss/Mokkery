package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.findExtensionParam
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.transformer.file.irGetMokkeryFileScope
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.isVararg

context(scope: TransformerScope)
fun IrCall.replaceVerifyNoMoreCalls(): IrExpression = this.replaceDeclarationIrBuilder {
    val originalCall = this@replaceVerifyNoMoreCalls
    val scopeParam = symbol.owner.findExtensionParam()
    val varargParam = symbol.owner.parameters.find { it.isVararg }
    irCall(referenced(MokkeryIr.Function.internalVerifyNoMoreCalls)) {
        arguments[0] = scopeParam?.let(originalCall.arguments::get) ?: irGetMokkeryFileScope()
        arguments[1] = varargParam?.let(originalCall.arguments::get)
    }
}
