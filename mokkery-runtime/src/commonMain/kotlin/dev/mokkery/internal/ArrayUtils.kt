@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.internal

import kotlin.reflect.KClass

@PublishedApi
internal fun KClass<*>.isArray(): Boolean = this == Array::class || bestName().startsWith("kotlin.Array")

internal fun Any?.asListOrNull(): List<Any?>? {
    return when (this) {
        is List<*> -> this
        is Array<*> -> asList()
        is IntArray -> asList()
        is ByteArray -> asList()
        is DoubleArray -> asList()
        is CharArray -> asList()
        is FloatArray -> asList()
        is LongArray -> asList()
        is BooleanArray -> asList()
        is ShortArray -> asList()
        is UIntArray -> asList()
        is UByteArray -> asList()
        is ULongArray -> asList()
        is UShortArray -> asList()
        else -> null
    }
}

@Suppress("UNCHECKED_CAST")
internal fun List<*>.toPlatformArrayOf(value: Any?): Any? {
    return when (value) {
        is Array<*> -> platformArrayOf(value::class, this)
        is IntArray -> (this as List<Int>).toIntArray()
        is ByteArray -> (this as List<Byte>).toByteArray()
        is DoubleArray -> (this as List<Double>).toDoubleArray()
        is CharArray -> (this as List<Char>).toCharArray()
        is FloatArray -> (this as List<Float>).toFloatArray()
        is LongArray -> (this as List<Long>).toLongArray()
        is BooleanArray -> (this as List<Boolean>).toBooleanArray()
        is ShortArray -> (this as List<Short>).toShortArray()
        is UIntArray -> (this as List<UInt>).toUIntArray()
        is UByteArray -> (this as List<UByte>).toUByteArray()
        is ULongArray -> (this as List<ULong>).toULongArray()
        is UShortArray -> (this as List<UShort>).toUShortArray()
        else -> null
    }
}

internal fun Any?.arrayElementType(): KClass<*> = when (this) {
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

internal expect fun platformArrayOf(kClass: KClass<*>, elements: List<Any?>): Array<*>
