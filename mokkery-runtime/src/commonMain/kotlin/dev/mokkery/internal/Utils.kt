package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallArg
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
@PublishedApi
internal inline fun <T> Any?.unsafeCast(): T = this as T

internal fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

internal fun callToString(
    receiver: String,
    name: String,
    args: List<CallArg>,
) = buildString {
    append(receiver)
    append(".")
    append(callFunctionToString(name, args))
}

internal fun callFunctionToString(name: String, args: List<CallArg>): String = buildString {
    append(name)
    append("(")
    append(args.joinToString { "${it.name} = ${it.description()}" })
    append(")")
}

internal fun generateMockId(typeName: String) = "$typeName(${Counter.mocks.next()})"

internal fun <T> List<T>.subListAfter(index: Int): List<T> {
    if (index >= size) return emptyList()
    return subList(index, size)
}

internal fun Any?.description(): String {
    if (this == null) return "null"
    if (this is String) return "\"$this\""
    if (this is Function<*>) return "{...}"
    val values = toListOrNull()
    if (values != null) return values.toString()
    return toString()
}

internal expect fun KClass<*>.bestName(): String
