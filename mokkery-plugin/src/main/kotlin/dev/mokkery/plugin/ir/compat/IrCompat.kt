package dev.mokkery.plugin.ir.compat

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrEnumEntrySymbol
import org.jetbrains.kotlin.ir.types.IrType

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

private val irGetEnumValueImplConstructor = IrGetEnumValueImpl::class
    .java
    .constructors
    .first { it.parameters.size == 5 }


private val irClassReferenceImplConstructor = IrClassReferenceImpl::class
    .java
    .constructors
    .first { it.parameters.size == 6 }

private val irFunctionExpressionImplConstructor = IrFunctionExpressionImpl::class
    .java
    .constructors
    .first { it.parameters.size == 6 }