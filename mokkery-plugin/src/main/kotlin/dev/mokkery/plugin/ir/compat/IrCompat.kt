package dev.mokkery.plugin.ir.compat

import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrMangleComputer

/**
 * Kotlin 2.4.20 added the `useEffectiveTypeVariances` constructor parameter to [IrMangleComputer].
 * Relying on default arguments binds to a synthetic constructor whose signature differs between
 * Kotlin versions, so all arguments are passed explicitly and the pre-2.4.20 constructor is
 * resolved reflectively as a fallback.
 */
fun IrMangleComputerCompat(
    builder: StringBuilder,
    mode: MangleMode,
    compatibleMode: Boolean,
): IrMangleComputer = try {
    IrMangleComputer(
        builder = builder,
        mode = mode,
        compatibleMode = compatibleMode,
        allowOutOfScopeTypeParameters = false,
        useEffectiveTypeVariances = false,
    )
} catch (_: NoSuchMethodError) {
    irMangleComputerConstructorOld.newInstance(builder, mode, compatibleMode, false)
}

private val irMangleComputerConstructorOld by lazy {
    IrMangleComputer::class.java.getConstructor(
        StringBuilder::class.java,
        MangleMode::class.java,
        Boolean::class.java,
        Boolean::class.java,
    )
}
