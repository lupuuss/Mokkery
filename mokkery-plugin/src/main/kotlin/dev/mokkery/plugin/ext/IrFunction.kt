package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.isTypeParameter

fun IrFunction.nonGenericReturnTypeOrAny(
    context: IrGeneratorContext
) = if (!returnType.isTypeParameter()) returnType else context.irBuiltIns.anyNType

fun IrValueParameter.nonGenericReturnTypeOrAny(
    context: IrGeneratorContext
) = if (!type.isTypeParameter()) type else context.irBuiltIns.anyNType

val IrFunction.mokkerySignature: String
    get() = "${name.asString()}/${valueParameters.joinToString { it.type.asString() }}/${returnType.asString()}"
