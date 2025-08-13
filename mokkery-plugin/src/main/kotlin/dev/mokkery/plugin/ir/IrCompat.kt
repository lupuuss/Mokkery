package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.util.copyTo

fun IrValueParameter.copyToCompat(
    irFunction: IrFunction,
    defaultValue: IrExpressionBody?,
    remapTypeMap: Map<IrTypeParameter, IrTypeParameter> = mapOf()
): IrValueParameter {
    return try {
        copyTo(irFunction, defaultValue = defaultValue, remapTypeMap = remapTypeMap)
    } catch (_: NoSuchMethodError) {
        Class.forName("org.jetbrains.kotlin.ir.util.IrUtilsKt")
            .methods
            .first { it.name == $$"copyTo$default" }
            .invoke(
                null,
                this,            // this
                irFunction,      // irFunction
                null,            // origin
                0,               // startOffset,
                0,               // endOffset
                null,            // name
                remapTypeMap,    // remapTypeMap
                null,            // type
                null,            // varargElementType
                defaultValue,    // defaultValue,
                false,           // isCrossline
                false,           // isNoinline
                false,           // isAssignable
                null,            // kind
                0b1111011011110, // mask
                null,            // marker
            ) as IrValueParameter
    }
}
