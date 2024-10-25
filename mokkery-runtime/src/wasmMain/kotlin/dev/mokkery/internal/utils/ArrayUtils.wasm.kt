package dev.mokkery.internal.utils

import kotlin.reflect.KClass

internal actual fun platformArrayOf(kClass: KClass<*>, elements: List<Any?>): Array<*> = elements.toTypedArray()
