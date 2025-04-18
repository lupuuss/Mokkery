package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.erasedUpperBound
import org.jetbrains.kotlin.ir.util.isFunction
import org.jetbrains.kotlin.ir.util.isSuspendFunction

fun IrType.isAnyFunction() = isFunction() || isSuspendFunction()

fun IrType.isPlatformDependent() = isPrimitiveType() || isArray() || isString()

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

fun IrType.isValueClassType(): Boolean = erasedUpperBound.isValue
