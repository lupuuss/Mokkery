package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.remapTypeParameters
import org.jetbrains.kotlin.utils.memoryOptimizedMap

fun IrSimpleFunction.copyReturnTypeFrom(
    function: IrSimpleFunction,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = mapOf()
) {
    returnType = function.returnType.remapTypeParameters(function, this, parameterMap)
}

fun IrSimpleFunction.copyNonDispatchParametersWithoutDefaultsFrom(
    function: IrSimpleFunction,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = mapOf()
) {
    parameters += function.nonDispatchParameters.memoryOptimizedMap { it.copyTo(this, defaultValue = null, remapTypeMap = parameterMap) }
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
