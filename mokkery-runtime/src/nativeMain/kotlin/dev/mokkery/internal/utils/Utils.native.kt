package dev.mokkery.internal.utils

import dev.mokkery.context.CallArgument
import kotlin.reflect.KClass

internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: ""

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun KClass<*>.takeIfImplementedOrAny(): KClass<*> = takeIfImplementedOrAnyImpl()

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun List<CallArgument>.copyWithReplacedKClasses(): List<CallArgument> = map {
    it.copy(type = it.parameter.type.takeIfImplementedOrAny())
}

private fun KClass<*>.takeIfImplementedOrAnyImpl() = takeIf { runCatching { hashCode() }.isSuccess } ?: Any::class

