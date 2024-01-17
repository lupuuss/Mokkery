package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.backend.jvm.ir.eraseTypeParameters
import org.jetbrains.kotlin.backend.jvm.ir.isCompiledToJvmDefault
import org.jetbrains.kotlin.config.JvmDefaultMode
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.DeepCopyIrTreeWithSymbols
import org.jetbrains.kotlin.ir.util.IrTypeParameterRemapper
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isFromJava
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentClassOrNull

fun IrSimpleFunction.copyAnnotationsFrom(function: IrSimpleFunction) {
    annotations = function.annotations.map { it.deepCopyWithSymbols(this) }
}

fun IrSimpleFunction.copyParametersFrom(function: IrSimpleFunction) {
    typeParameters = function.typeParameters.map { it.deepCopyWithSymbols(this) }
    val mapper = IrTypeParameterRemapper(function.typeParameters.zip(typeParameters).toMap())
    fun IrValueParameter.deepCopyWithTypeParameters(): IrValueParameter =
        deepCopyWithSymbols(this@copyParametersFrom) { symbolRemapper, _ ->
            DeepCopyIrTreeWithSymbols(symbolRemapper, mapper)
        }
    extensionReceiverParameter = function.extensionReceiverParameter?.deepCopyWithTypeParameters()
    valueParameters = function.valueParameters.map { it.deepCopyWithTypeParameters().apply { defaultValue = null } }
}

fun IrFunction.eraseFullValueParametersList() = fullValueParameterList.forEach { param ->
    val consumedParams = param.type.extractAllConsumedTypeParameters()
    if (consumedParams.any { it in typeParameters }) return@forEach
    param.type = param.type.eraseTypeParameters()
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
