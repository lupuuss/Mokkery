package dev.mokkery.plugin.core.ir

import dev.mokkery.plugin.core.MokkeryCore
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.constructors

fun IrClass.findMokkeryConstructor(): IrConstructor? = constructors.find { it.isMokkeryConstructor() }

fun IrConstructor.isMokkeryConstructor(): Boolean {
    val param = parameters.find { it.kind == IrParameterKind.Regular } ?: return false
    return param.name == MokkeryCore.Names.mockableConstructorMarkerParam && param.type.isUnit()
}
