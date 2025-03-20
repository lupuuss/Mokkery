package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.remapTypeParameters
import org.jetbrains.kotlin.utils.memoryOptimizedMap

fun IrSimpleFunction.copyReturnTypeFrom(
    function: IrSimpleFunction,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = mapOf()
) {
    returnType = function.returnType.remapTypeParameters(function, this, parameterMap)
}

fun IrSimpleFunction.copyParametersFrom(
    function: IrSimpleFunction,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = mapOf()
) {
    extensionReceiverParameter = function.extensionReceiverParameter?.copyToCompat(this, remapTypeMap = parameterMap)
    valueParameters = function.valueParameters
        .memoryOptimizedMap { it.copyToCompat(this, defaultValue = null, remapTypeMap = parameterMap) }
}

fun IrSimpleFunction.isSuperCallFor(originalFunction: IrFunction): Boolean {
    if (modality != Modality.OPEN) return false
    val parent = parentClassOrNull ?: return false
    val originalFunctionParentSupertypes = originalFunction.parentClassOrNull
        ?.superTypes
        ?.memoryOptimizedMap { it.eraseTypeParametersCompat() }
        .orEmpty()
    return parent.defaultTypeErased in originalFunctionParentSupertypes
}
