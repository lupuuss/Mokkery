@file:JvmName("JvmUtilsKt")
package dev.mokkery.internal

import kotlin.jvm.JvmName
import kotlin.reflect.KClass

internal actual fun KClass<*>.bestName(): String = qualifiedName ?: simpleName ?: ""
