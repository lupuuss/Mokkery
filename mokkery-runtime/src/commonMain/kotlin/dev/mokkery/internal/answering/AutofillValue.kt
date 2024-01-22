package dev.mokkery.internal.answering

import dev.mokkery.internal.DefaultNothingException
import dev.mokkery.internal.isArray
import dev.mokkery.internal.platformArrayOf
import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass

internal expect fun autofillAny(kClass: KClass<*>): Any?

internal fun <T> autofillValue(returnType: KClass<*>): T = when {
    returnType == Nothing::class -> throw DefaultNothingException()
    returnType.isArray() -> platformArrayOf(returnType, listOf(null)).unsafeCast()
    else -> (autofillMapping[returnType] ?: autofillAny(returnType)) as T
}

@OptIn(ExperimentalUnsignedTypes::class)
internal val autofillMapping = mapOf(
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
    Boolean::class to false,
    Unit::class to Unit,
    KClass::class to Any::class,
    String::class to "",
    IntArray::class to intArrayOf(0),
    ByteArray::class to byteArrayOf(0),
    DoubleArray::class to doubleArrayOf(0.0),
    CharArray::class to charArrayOf(0.toChar()),
    FloatArray::class to floatArrayOf(0.0f),
    LongArray::class to longArrayOf(0),
    BooleanArray::class to booleanArrayOf(false),
    ShortArray::class to shortArrayOf(0),
    UIntArray::class to uintArrayOf(0u),
    UByteArray::class to ubyteArrayOf(0u),
    ULongArray::class to ulongArrayOf(0u),
    UShortArray::class to ushortArrayOf(0u)
)
