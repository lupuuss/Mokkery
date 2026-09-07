package dev.mokkery.internal.utils

import kotlin.reflect.KClass

internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: ""

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun KClass<*>.takeIfImplementedOrAny(): KClass<*> = takeIfImplementedOrAnyImpl()

private fun KClass<*>.takeIfImplementedOrAnyImpl() = takeIf { runCatching { hashCode() }.isSuccess } ?: Any::class

