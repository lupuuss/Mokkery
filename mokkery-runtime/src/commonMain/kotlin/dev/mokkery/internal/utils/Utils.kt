package dev.mokkery.internal.utils

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.context.CallArgument
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.MokkeryIntrinsicException
import kotlin.reflect.KClass

@PublishedApi
internal val mokkeryIntrinsic: Nothing
    get() = throw MokkeryIntrinsicException()

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
@PublishedApi
internal inline fun <T> Any?.unsafeCast(): T = this as T

internal fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

internal fun callToString(
    id: MokkeryInstanceId,
    name: String,
    args: List<CallArgument>,
) = buildString {
    append(id)
    append(".")
    append(callFunctionToString(name, args))
}

internal fun callFunctionToString(
    name: String,
    args: List<CallArgument>,
) = PropertyDescriptor.fromNameOrNull(name)
    ?.toCallString(args.map { it.value.description() })
    ?: buildString {
        append(name)
        append("(")
        append(args.joinToString { "${it.parameter.name} = ${it.value.description()}" })
        append(")")
    }

internal fun Any?.description(): String {
    if (this == null) return "null"
    if (this is String) return "\"$this\""
    if (this is Function<*>) return "{...}"
    val values = asListOrNull()
    if (values != null) return values.toString()
    return toString()
}

internal fun mokkeryRuntimeError(message: String): Nothing = throw MokkeryRuntimeException(message)

internal expect fun KClass<*>.bestName(): String

/*
 * It's only relevant for the K/N, because KClass instances of interop
 * types are not actually implemented - all methods fail. In this case KClass of Any is return
 * to proceed without exceptions.
 *
 * TODO Remove when KClass is supported for interop types.
 */
internal expect inline fun KClass<*>.takeIfImplementedOrAny(): KClass<*>

/*
 * Similarly to takeIfImplementedOrAny only relevant for K/N. It avoids copying List<CallArgs> on other platforms.
 */
internal expect inline fun List<CallArgument>.copyWithReplacedKClasses(): List<CallArgument>
