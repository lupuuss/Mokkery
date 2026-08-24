package dev.mokkery.internal.utils

import kotlin.reflect.KClass

internal actual fun KClass<*>.bestName(): String = simpleName.orEmpty()

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun KClass<*>.takeIfImplementedOrAny(): KClass<*> = this
