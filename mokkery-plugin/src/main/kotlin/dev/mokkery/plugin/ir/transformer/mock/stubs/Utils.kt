package dev.mokkery.plugin.ir.transformer.mock.stubs

import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.irCall
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.util.dumpKotlinLike

context(scope: StubStrategyScope)
fun IrBlockBodyBuilder.stubFunctionBody(func: IrSimpleFunction) {
    context(scope.with(this)) {
        val value = strategy.provide(func.returnType)
        if (value == null) {
            +irCallError(
                "Mokkery stub value could not be provided for type `${func.returnType.dumpKotlinLike()}`!"
            )
        } else {
            +irReturn(value.expression)
        }
    }
}


context(scope: StubStrategyScope)
private fun IrBlockBodyBuilder.irCallError(
    message: String
): IrCall = irCall(referenced(MokkeryIr.Function.mokkeryRuntimeError)) {
    arguments[0] = irString(message)
}
