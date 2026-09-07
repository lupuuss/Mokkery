package dev.mokkery.plugin.ir

import dev.mokkery.plugin.ir.compat.IrMangleComputerCompat
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.ir.declarations.IrDeclaration

fun IrDeclaration.computeSignature(): String = IrMangleComputerCompat(StringBuilder(256), MangleMode.SIGNATURE, true)
    .computeMangle(this)
