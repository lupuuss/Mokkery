package dev.mokkery.internal

internal inline fun <reified T> Any?.unsafeCast() = this as T

internal fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
