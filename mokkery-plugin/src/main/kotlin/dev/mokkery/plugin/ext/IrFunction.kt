package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.util.isTypeParameter

fun IrFunction.nonGenericReturnTypeOrAny(
    context: IrGeneratorContext
) = if (!returnType.isTypeParameter()) returnType else context.irBuiltIns.anyNType

