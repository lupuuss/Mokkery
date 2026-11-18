package dev.mokkery.plugin.ir.compat

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression

expect fun IrExpression.extractTemplatingReference(): Pair<IrExpression, IrSimpleFunction>?
