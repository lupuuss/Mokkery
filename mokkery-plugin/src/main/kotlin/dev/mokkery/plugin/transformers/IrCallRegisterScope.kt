package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.ext.getProperty
import dev.mokkery.plugin.ext.irCall
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.getSimpleFunction

fun IrBuilderWithScope.irCallRegisterScope(
    transformer: TransformerScope,
    mokkeryScope: IrExpression,
    obj: IrExpression
): IrCall {
    val lookUpClass = transformer.getClass(Mokkery.Class.MokkeryScopeLookup)
    val lookUpCompanion = lookUpClass.companionObject()!!
    val currentLookupCall = irCall(lookUpCompanion.getProperty("current").getter!!) {
        dispatchReceiver = irGetObject(lookUpCompanion.symbol)
    }
    val registerCall = irCall(lookUpClass.getSimpleFunction("register")!!) {
        dispatchReceiver = currentLookupCall
        putValueArgument(0, obj)
        putValueArgument(1, mokkeryScope)
    }
    return registerCall
}
