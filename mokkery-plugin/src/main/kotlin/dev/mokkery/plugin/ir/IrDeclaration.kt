package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrMangleComputer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration

fun IrDeclaration.computeSignature(): String = IrMangleComputer(StringBuilder(256), MangleMode.SIGNATURE, true)
    .computeMangle(this)
