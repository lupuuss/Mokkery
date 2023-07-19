package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isSuspendFunction

fun IrType.isAnyFunction() = isFunction() || isSuspendFunction()
