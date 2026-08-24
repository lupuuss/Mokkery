package dev.mokkery.plugin.ir.compat

import org.jetbrains.kotlin.DeprecatedCompilerApi
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrMangleComputer
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.parentAsClass
import java.lang.reflect.Constructor

fun irMangleComputerCompat(
    builder: StringBuilder,
    mode: MangleMode
): IrMangleComputer = when (val constructor = legacyMangleComputerConstructor) {
    null -> IrMangleComputer(builder, mode, true)
    else -> constructor.newInstance(builder, mode, true, false) as IrMangleComputer
}

val IrAnnotation.classSymbolCompat: IrClassSymbol
    get() = try {
        classSymbol
    } catch (_: NoSuchMethodError) {
        @OptIn(DeprecatedCompilerApi::class)
        symbol.owner.parentAsClass.symbol
    }


private val legacyMangleComputerConstructor: Constructor<*>? by lazy {
    try {
        IrMangleComputer(StringBuilder(0), MangleMode.SIGNATURE, true)
        null
    } catch (e: NoSuchMethodError) {
        IrMangleComputer::class.java
            .declaredConstructors
            .firstOrNull { it.parameterCount == 4 } ?: throw e
    }
}
