package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isSuspendFunction
import org.jetbrains.kotlin.utils.memoryOptimizedFlatMap
import org.jetbrains.kotlin.utils.memoryOptimizedMap

fun IrType.isAnyFunction() = isFunction() || isSuspendFunction()

val IrType.argumentTypes: List<IrType?>
    get() = (this as? IrSimpleType)?.arguments?.memoryOptimizedMap { it.typeOrNull }.orEmpty()

fun List<IrType>.flattenArgumentTypes(): List<IrType?> = memoryOptimizedFlatMap { it.argumentTypes }

val IrTypeParameter.erasedUpperBound: IrType get() = defaultType.eraseTypeParameters()

fun IrTypeParametersContainer.typeArgumentsFrom(typeArguments: List<IrType?>): List<IrType> = typeParameters
    .mapIndexed { index, param -> typeArguments.getOrNull(index) ?: param.erasedUpperBound }

fun IrTypeParametersContainer.typeSubstitutionFrom(
    typeArguments: List<IrType?>
): Map<IrTypeParameterSymbol, IrType> = typeParameters
    .memoryOptimizedMap { it.symbol }
    .zip(typeArgumentsFrom(typeArguments))
    .toMap()

val IrType.typeSubstitution: Map<IrTypeParameterSymbol, IrType>
    get() = classOrNull?.owner?.typeSubstitutionFrom(argumentTypes).orEmpty()

fun IrType.extractAllConsumedTypeParameters(): List<IrTypeParameter> {
    val param = asTypeParamOrNull()
    return when {
        param != null -> listOf(param)
        this is IrSimpleType -> arguments.flatMap { if (it is IrType) it.extractAllConsumedTypeParameters() else emptyList() }
        else -> emptyList()
    }
}

fun IrType.asTypeParamOrNull() = classifierOrNull
    .let { it as? IrTypeParameterSymbol }
    ?.owner
