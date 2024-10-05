package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass

internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: ""

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun KClass<*>.takeIfImplementedOrAny(): KClass<*> = takeIfImplementedOrAnyImpl()

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun List<CallArg>.copyWithReplacedKClasses(): List<CallArg> = map {
    it.copy(type = it.type.takeIfImplementedOrAny())
}

private fun KClass<*>.takeIfImplementedOrAnyImpl() = takeIf { runCatching { hashCode() }.isSuccess } ?: Any::class

