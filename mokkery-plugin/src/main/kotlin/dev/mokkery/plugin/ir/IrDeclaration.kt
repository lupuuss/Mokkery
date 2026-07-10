package dev.mokkery.plugin.ir

import dev.mokkery.plugin.ir.compat.irMangleComputerCompat
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.ir.declarations.IrDeclaration

fun IrDeclaration.computeSignature(): String = irMangleComputerCompat(StringBuilder(256), MangleMode.SIGNATURE)
    .computeMangle(this)
