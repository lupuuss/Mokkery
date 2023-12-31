package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.backend.jvm.ir.eraseTypeParameters
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.DeepCopyIrTreeWithSymbols
import org.jetbrains.kotlin.ir.util.IrTypeParameterRemapper
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols

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
