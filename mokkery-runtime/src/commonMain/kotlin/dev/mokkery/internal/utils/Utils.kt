package dev.mokkery.internal.utils

import dev.mokkery.context.CallArgument
import kotlin.reflect.KClass

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

/*
 * Similarly to takeIfImplementedOrAny only relevant for K/N. It avoids copying List<CallArgs> on other platforms.
 */
internal expect inline fun List<CallArgument>.copyWithReplacedKClasses(): List<CallArgument>
