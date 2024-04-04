package dev.mokkery.test

import kotlin.jvm.JvmInline

@JvmInline
value class ValueClass<T>(val value: T)

@JvmInline
value class PrimitiveValueClass(val value: Int)
