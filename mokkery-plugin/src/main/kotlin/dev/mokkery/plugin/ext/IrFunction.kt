package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.backend.jvm.ir.eraseTypeParameters
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.isTypeParameter

fun IrFunction.nonGenericReturnTypeOrAny(
    context: IrGeneratorContext
) = if (!returnType.isTypeParameter()) returnType else context.irBuiltIns.anyNType

fun IrValueParameter.nonGenericReturnTypeOrAny(
    context: IrGeneratorContext
) = if (!type.isTypeParameter()) type else context.irBuiltIns.anyNType

fun IrFunction.eraseFullValueParametersList() = fullValueParameterList.forEach {
    val typeClassifier = it.type.classifierOrNull
    if (typeClassifier !is IrTypeParameterSymbol || typeClassifier.owner !in typeParameters) {
        it.type = it.type.eraseTypeParameters()
    }
}
