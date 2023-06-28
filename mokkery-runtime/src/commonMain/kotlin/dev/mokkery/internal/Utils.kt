package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
@PublishedApi
internal inline fun <T> Any?.unsafeCast(): T = this as T

internal fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

internal fun generateSignature(
    name: String,
    args: List<CallArg>
): String = "$name(${args.joinToString { "${it.name}: ${it.type.simpleName}" }})"
