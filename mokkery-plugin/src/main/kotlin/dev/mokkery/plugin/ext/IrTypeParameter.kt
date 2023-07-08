package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.util.copyToWithoutSuperTypes

fun IrTypeParameter.copyTo(target: IrTypeParametersContainer) = copyToWithoutSuperTypes(target).also {
    it.superTypes = superTypes
}
