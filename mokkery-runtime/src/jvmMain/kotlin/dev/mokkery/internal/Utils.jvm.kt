package dev.mokkery.internal

import kotlin.reflect.KClass

internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: ""

