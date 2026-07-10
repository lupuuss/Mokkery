package dev.mokkery.plugin.ir.compat

import org.jetbrains.kotlin.DeprecatedCompilerApi
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrMangleComputer
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.parentAsClass

fun irMangleComputerCompat(
    builder: StringBuilder,
    mode: MangleMode
): IrMangleComputer = try {
    IrMangleComputer(builder, mode, true)
} catch (e: NoSuchMethodError) {
    val constructor = IrMangleComputer::class.java
        .declaredConstructors
        .firstOrNull { it.parameterCount == 4 } ?: throw e
    return constructor.newInstance(builder, mode, true, false) as IrMangleComputer
}

val IrAnnotation.classSymbolCompat: IrClassSymbol
    get() = try {
        classSymbol
    } catch (_: NoSuchMethodError) {
        @OptIn(DeprecatedCompilerApi::class)
        symbol.owner.parentAsClass.symbol
    }
