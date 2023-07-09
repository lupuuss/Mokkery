package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.backend.jvm.ir.eraseTypeParameters
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.DeepCopyIrTreeWithSymbols
import org.jetbrains.kotlin.ir.util.IrTypeParameterRemapper
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.isTypeParameter

fun IrFunction.nonGenericReturnTypeOrAny(
    context: IrGeneratorContext
) = if (!returnType.isTypeParameter()) returnType else context.irBuiltIns.anyNType

fun IrValueParameter.nonGenericReturnTypeOrAny(
    context: IrGeneratorContext
) = if (!type.isTypeParameter()) type else context.irBuiltIns.anyNType


fun IrSimpleFunction.copyAnnotationsFrom(function: IrSimpleFunction) {
    annotations = function.annotations.map { it.deepCopyWithSymbols(this) }
}

fun IrSimpleFunction.copyParametersFrom(function: IrSimpleFunction) {
    typeParameters = function.typeParameters.map { it.deepCopyWithSymbols(this) }
    val mapper = IrTypeParameterRemapper(function.typeParameters.zip(typeParameters).toMap())
    fun IrValueParameter.deepCopyWithTypeParameters(): IrValueParameter = deepCopyWithSymbols(this@copyParametersFrom) { symbolRemapper, _ ->
        DeepCopyIrTreeWithSymbols(symbolRemapper, mapper)
    }
    extensionReceiverParameter = function.extensionReceiverParameter?.deepCopyWithTypeParameters()
    valueParameters = function.valueParameters.map { it.deepCopyWithTypeParameters().apply { defaultValue = null } }
}

fun IrFunction.eraseFullValueParametersList() = fullValueParameterList.forEach {
    val typeClassifier = it.type.classifierOrNull
    if (typeClassifier !is IrTypeParameterSymbol || typeClassifier.owner !in typeParameters) {
        it.type = it.type.eraseTypeParameters()
    }
}
