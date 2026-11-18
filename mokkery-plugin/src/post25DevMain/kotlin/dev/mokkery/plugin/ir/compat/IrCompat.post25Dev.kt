package dev.mokkery.plugin.ir.compat

import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrRichFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrRichPropertyReference

actual fun IrExpression.extractTemplatingReference(): Pair<IrExpression, IrSimpleFunction>? {
    if (this !is IrRichFunctionReference) return null
    // it handles 2 cases:
    // * this::property::get/this::property::set
    // * this::function
    val target = reflectionTargetSymbol!!.owner
    return when (val dispatchArgument = boundValues[0]) {
        is IrRichPropertyReference -> {
            val property = dispatchArgument.reflectionTargetSymbol!!.owner as IrProperty
            dispatchArgument.boundValues[0] to when {
                target.name.asString() == "get" -> property.getter!!
                target.name.asString() == "set" -> property.setter!!
                else -> error("Only reference to getter and setter is allowed on property!")
            }
        }
        else -> dispatchArgument to target as IrSimpleFunction
    }
}
