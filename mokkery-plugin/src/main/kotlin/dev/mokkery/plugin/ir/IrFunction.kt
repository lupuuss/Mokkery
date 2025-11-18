package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.remapTypeParameters
import org.jetbrains.kotlin.utils.memoryOptimizedMap

fun IrFunction.copyReturnTypeFrom(
    function: IrFunction,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = mapOf()
) {
    returnType = function.returnType.remapTypeParameters(function, this, parameterMap)
}

fun IrFunction.copyNonDispatchParametersWithoutDefaultsFrom(
    function: IrFunction,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = mapOf()
) {
    parameters += function.nonDispatchParameters.memoryOptimizedMap {
        it.copyToCompat(this, defaultValue = null, remapTypeMap = parameterMap)
    }
}

fun IrSimpleFunction.isSuperCallFor(originalFunction: IrFunction): Boolean {
    if (modality != Modality.OPEN) return false
    val parent = parentClassOrNull ?: return false
    val originalFunctionParentSupertypes = originalFunction.parentClassOrNull
        ?.superTypes
        ?.memoryOptimizedMap { it.eraseTypeParameters() }
        .orEmpty()
    return parent.defaultTypeErased in originalFunctionParentSupertypes
}
