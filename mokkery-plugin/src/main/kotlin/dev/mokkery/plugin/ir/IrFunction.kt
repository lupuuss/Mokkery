package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.backend.jvm.ir.isCompiledToJvmDefault
import org.jetbrains.kotlin.config.JvmDefaultMode
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isFromJava
import org.jetbrains.kotlin.ir.util.isInterface
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
    extensionReceiverParameter = function.extensionReceiverParameter?.copyTo(this, remapTypeMap = parameterMap)
    valueParameters = function.valueParameters
        .memoryOptimizedMap { it.copyTo(this, defaultValue = null, remapTypeMap = parameterMap) }
}

fun IrSimpleFunction.isJvmBinarySafeSuperCall(
    originalFunction: IrFunction,
    jvmDefaultMode: JvmDefaultMode,
    allowIndirectSuperCalls: Boolean
): Boolean {
    if (modality != Modality.OPEN) return false
    val parent = parentClassOrNull ?: return false
    val originalFunctionParentSupertypes = originalFunction.parentClassOrNull?.superTypes.orEmpty()
    if (parent.defaultType in originalFunctionParentSupertypes) return true
    if (!allowIndirectSuperCalls) return false
    if (isFakeOverride) return false
    if (isFromJava()) return false
    if (parent.isInterface && !isFakeOverride && isCompiledToJvmDefault(jvmDefaultMode)) return false
    return true
}
