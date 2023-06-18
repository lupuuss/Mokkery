package dev.mokkery.internal.answering

import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass

internal fun <T> autofillValue(returnType: KClass<*>): T = autoFillMap[returnType].unsafeCast()

@OptIn(ExperimentalUnsignedTypes::class)
private val autoFillMap = mapOf(
    Byte::class to 0.toByte(),
    UByte::class to 0u.toUByte(),
    Char::class to '0',
    Short::class to 0.toShort(),
    UShort::class to 0.toUShort(),
    Int::class to 0,
    UInt::class to 0u,
    Long::class to 0L,
    ULong::class to 0uL,
    Double::class to 0.0,
    Float::class to 0f,
    Unit::class to Unit,
    KClass::class to Any::class,
    String::class to "",
    Array::class to emptyArray<Any?>(),
    IntArray::class to intArrayOf(),
    ByteArray::class to byteArrayOf(),
    DoubleArray::class to doubleArrayOf(),
    CharArray::class to charArrayOf(),
    FloatArray::class to floatArrayOf(),
    LongArray::class to longArrayOf(),
    BooleanArray::class to booleanArrayOf(),
    ShortArray::class to shortArrayOf(),
    UIntArray::class to uintArrayOf(),
    UByteArray::class to ubyteArrayOf(),
    ULongArray::class to ulongArrayOf(),
    UShortArray::class to ushortArrayOf()
)
