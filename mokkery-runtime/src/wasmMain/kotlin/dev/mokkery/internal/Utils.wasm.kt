package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass

internal actual fun KClass<*>.bestName(): String = simpleName.orEmpty()

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun KClass<*>.takeIfImplementedOrAny(): KClass<*> = this

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun List<CallArg>.copyWithReplacedKClasses(): List<CallArg> = this
