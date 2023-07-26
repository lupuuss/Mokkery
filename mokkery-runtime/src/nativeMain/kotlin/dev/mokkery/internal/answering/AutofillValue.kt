package dev.mokkery.internal.answering

import kotlin.reflect.KClass

private object UnsafeValue

internal actual  fun autofillAny(kClass: KClass<*>): Any? = UnsafeValue
