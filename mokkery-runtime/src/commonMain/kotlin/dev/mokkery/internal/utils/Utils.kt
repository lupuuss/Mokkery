package dev.mokkery.internal.utils

import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
@PublishedApi
internal inline fun <T> Any?.unsafeCast(): T = this as T

internal expect fun KClass<*>.bestName(): String

/*
 * It's only relevant for the K/N, because KClass instances of interop
 * types are not actually implemented - all methods fail. In this case KClass of Any is return
 * to proceed without exceptions.
 *
 * TODO Remove when KClass is supported for interop types.
 */
internal expect inline fun KClass<*>.takeIfImplementedOrAny(): KClass<*>

@PublishedApi
@Suppress("unused")
internal fun KType.getTypeArgumentClassOrNull(
    index: Int
): KClass<*>? = arguments
    .getOrNull(index)
    ?.type
    ?.classifier as? KClass<*>
