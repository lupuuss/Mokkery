@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.internal

import kotlin.reflect.KClass

internal fun Any?.toListOrNull(): List<Any?>? {
    return  when (this) {
        is Iterable<*> -> toList()
        is Array<*> -> toList()
        is IntArray -> toList()
        is ByteArray -> toList()
        is DoubleArray -> toList()
        is CharArray -> toList()
        is FloatArray -> toList()
        is LongArray -> toList()
        is BooleanArray -> toList()
        is ShortArray -> toList()
        is UIntArray -> toList()
        is UByteArray -> toList()
        is ULongArray -> toList()
        is UShortArray -> toList()
        else -> null
    }
}

internal fun Any?.arrayElementType(): KClass<*>  = when (this) {
    is Array<*> -> Any::class
    is IntArray -> Int::class
    is ByteArray -> Byte::class
    is DoubleArray -> Double::class
    is CharArray -> Char::class
    is FloatArray -> Float::class
    is LongArray -> Long::class
    is BooleanArray -> Boolean::class
    is ShortArray -> Short::class
    is UIntArray -> UInt::class
    is UByteArray -> UByte::class
    is ULongArray -> ULong::class
    is UShortArray -> UShort::class
    else -> Any::class
}

internal fun varargNameByElementType(cls: KClass<*>): String = when (cls) {
    Int::class -> "varargsInt"
    UInt::class -> "varargsUInt"
    Short::class -> "varargsShort"
    UShort::class -> "varargsUShort"
    Byte::class -> "varargsByte"
    UByte::class -> "varargsUByte"
    Char::class -> "varargsChar"
    Double::class -> "varargsDouble"
    Float::class -> "varargsFloat"
    ULong::class -> "varargsULong"
    Long::class -> "varargsLong"
    Boolean::class -> "varargsBoolean"
    Any::class -> "varargs"
    else -> "varargs<${cls.simpleName}>"
}
