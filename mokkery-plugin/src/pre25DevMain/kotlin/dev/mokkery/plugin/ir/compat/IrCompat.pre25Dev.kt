package dev.mokkery.plugin.ir.compat

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference

actual fun IrExpression.extractTemplatingReference(): Pair<IrExpression, IrSimpleFunction>? {
    if (this !is IrFunctionReference) return null
    // it handles 2 cases:
    // * this::property::get/this::property::set
    // * this::function
    return when (val dispatchArgument = arguments[0]!!) {
        is IrPropertyReference -> dispatchArgument.arguments[0]!! to when {
            symbol.owner.name.asString() == "get" -> dispatchArgument.getter!!.owner
            symbol.owner.name.asString() == "set" -> dispatchArgument.setter!!.owner
            else -> error("Only reference to getter and setter is allowed on property!")
        }
        else -> dispatchArgument to symbol.owner as IrSimpleFunction
    }
}
