package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.ext.irCall
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.getPropertyGetter

fun IrBlockBodyBuilder.irCallIsTemplatingEnabled(
    transformer: TransformerScope,
    mokkeryScope: IrExpression
): IrCall {
    val mokkerySpyClass = transformer.getClass(Mokkery.Class.MokkerySpy)
    val mokkerySpyScopeClass = transformer.getClass(Mokkery.Class.MokkerySpyScope)
    val getTemplatingCall = irCall(mokkerySpyClass.symbol.getPropertyGetter("templating")!!)
    getTemplatingCall.dispatchReceiver = irCall(mokkerySpyScopeClass.getPropertyGetter("interceptor")!!) {
        dispatchReceiver = mokkeryScope
    }
    val templatingInterceptorClass = transformer.getClass(Mokkery.Class.TemplatingInterceptor)
    val isEnabledCall = irCall(templatingInterceptorClass.getPropertyGetter("isEnabled")!!) {
        dispatchReceiver = getTemplatingCall
    }
    return isEnabledCall
}
