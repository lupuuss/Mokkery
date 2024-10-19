package dev.mokkery.plugin.ir.compat

import dev.mokkery.plugin.ir.irIfNotNull
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrEnumEntrySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.copyTo

fun IrGetEnumValueImplCompat(
    startOffset: Int,
    endOffset: Int,
    type: IrType,
    symbol: IrEnumEntrySymbol,
): IrGetEnumValueImpl {
    return try {
        IrGetEnumValueImpl(startOffset, endOffset, type, symbol)
    } catch (e: NoClassDefFoundError) {
        irGetEnumValueImplConstructor
            .newInstance(null, startOffset, endOffset, type, symbol)
            .let { it as IrGetEnumValueImpl }
    }
}

fun IrClassReferenceImplCompat(
    startOffset: Int,
    endOffset: Int,
    type: IrType,
    symbol: IrClassifierSymbol,
    classType: IrType,
): IrClassReferenceImpl {
    return try {
        IrClassReferenceImpl(startOffset, endOffset, type, symbol, classType)
    } catch (e: NoClassDefFoundError) {
        irClassReferenceImplConstructor
            .newInstance(null, startOffset, endOffset, type, symbol, classType)
            .let { it as IrClassReferenceImpl }
    }
}

fun IrFunctionExpressionImplCompat(
    startOffset: Int,
    endOffset: Int,
    type: IrType,
    function: IrSimpleFunction,
    origin: IrStatementOrigin,
): IrFunctionExpressionImpl {
    return try {
        IrFunctionExpressionImpl(startOffset, endOffset, type, function, origin)
    } catch (e: NoClassDefFoundError) {
        irFunctionExpressionImplConstructor
            .newInstance(null, startOffset, endOffset, type, function, origin)
            .let { it as IrFunctionExpressionImpl }
    }
}

fun IrCallImplCompat(
    startOffset: Int,
    endOffset: Int,
    type: IrType,
    origin: IrStatementOrigin?,
    typeArgumentsCount: Int,
    valueArgumentsCount: Int,
    symbol: IrSimpleFunctionSymbol,
    superQualifierSymbol: IrClassSymbol?
): IrCallImpl = try {
    IrCallImpl(
        startOffset = startOffset,
        endOffset = endOffset,
        type = type,
        symbol = symbol,
        typeArgumentsCount = typeArgumentsCount,
        origin = origin,
        superQualifierSymbol = superQualifierSymbol
    )
} catch (e: LinkageError) {
    irClassImplConstructor.newInstance(
        startOffset,
        endOffset,
        type,
        symbol,
        typeArgumentsCount,
        valueArgumentsCount,
        origin,
        superQualifierSymbol
    ) as IrCallImpl
}

fun IrBuilderWithScope.irIfThenCompat(
    condition: IrExpression,
    thenPart: IrExpression
): IrWhen {
    return try {
        irIfThen(condition = condition, thenPart = thenPart)
    } catch (e: NoSuchMethodError) {
        irIfThenFunction
            .invoke(null, this, condition, thenPart)
            .let { it as IrWhen }
    }
}

fun IrBuilderWithScope.irIfNotNullCompat(arg: IrExpression, then: IrExpression): IrWhen {
    return try {
        irIfNotNull(arg, then)
    } catch (e: NoSuchMethodError) {
        irIfNotNullFunction
            .invoke(null, this, arg, then)
            .let { it as IrWhen }
    }
}

fun IrValueParameter.copyToCompat(
    irFunction: IrFunction,
    defaultValue: IrExpressionBody? = this.defaultValue,
): IrValueParameter {
    return try {
        copyTo(irFunction = irFunction, defaultValue = defaultValue)
    } catch (e: NoSuchMethodError) {
        irCopyTo
            .invoke(
                null,
                this,
                irFunction,
                null,
                0,
                0,
                0,
                null,
                null,
                null,
                null,
                defaultValue,
                false,
                false,
                false,
                0b1110111111110, // binary mask for method default, zeros indicate arguments that should be accepted
                null
            ) as IrValueParameter
    }
}

private val irClassImplConstructor by lazy {
    IrCallImpl::class
        .java
        .constructors
        .first { it.parameters.size == 8 }
}

private val irGetEnumValueImplConstructor by lazy {
    IrGetEnumValueImpl::class
        .java
        .constructors
        .first { it.parameters.size == 5 }
}


private val irClassReferenceImplConstructor by lazy {
    IrClassReferenceImpl::class
        .java
        .constructors
        .first { it.parameters.size == 6 }
}

private val irFunctionExpressionImplConstructor by lazy {
    IrFunctionExpressionImpl::class
        .java
        .constructors
        .first { it.parameters.size == 6 }
}

private val irIfThenFunction by lazy {
    "org.jetbrains.kotlin.backend.common.lower.LowerUtilsKt"
        .loadClass()
        .methods
        .first { it.name == "irIfThen" && it.parameters.size == 3 }
}

private val irIfNotNullFunction by lazy {
    "org.jetbrains.kotlin.backend.common.lower.LowerUtilsKt"
        .loadClass()
        .methods
        .first { it.name == "irIfNotNull" && it.parameters.size == 3 }
}

private val irCopyTo by lazy {
    "org.jetbrains.kotlin.ir.util.IrUtilsKt"
        .loadClass()
        .methods
        .first { it.name == "copyTo\$default" }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun String.loadClass() = ClassLoader.getSystemClassLoader().loadClass(this)
