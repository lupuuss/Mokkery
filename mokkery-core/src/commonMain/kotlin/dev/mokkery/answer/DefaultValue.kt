package dev.mokkery.answer

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
internal fun <T> defaultValue(returnType: KClass<*>): T = when (returnType) {
    Byte::class -> 0.toByte()
    UByte::class -> 0u.toUByte()
    Char::class -> '0'
    Short::class -> 0.toShort()
    UShort::class -> 0.toUShort()
    Int::class -> 0
    UInt::class -> 0u
    Long::class -> 0L
    ULong::class -> 0uL
    Double::class -> 0.0
    Float::class -> 0f
    Unit::class -> Unit
    KClass::class -> Any::class
    String::class -> ""
    else -> null
} as T

