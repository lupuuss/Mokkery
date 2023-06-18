package dev.mokkery.internal

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
@PublishedApi
internal inline fun <T> Any?.unsafeCast(): T = this as T

internal fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
