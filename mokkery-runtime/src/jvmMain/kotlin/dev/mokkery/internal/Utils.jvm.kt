package dev.mokkery.internal

import dev.mokkery.context.CallArgument
import kotlin.reflect.KClass

internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: ""

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun KClass<*>.takeIfImplementedOrAny(): KClass<*> = this

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun List<CallArgument>.copyWithReplacedKClasses(): List<CallArgument> = this
