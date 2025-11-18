@file:Suppress("NOTHING_TO_INLINE", "unused")

package dev.mokkery.internal.defaults

import dev.mokkery.internal.utils.mokkeryRuntimeError

internal inline fun methodWithoutDefaultsError(): Nothing = mokkeryRuntimeError(
    "This method does not have defaults and should not be called!"
)

internal inline fun throwArguments(vararg arguments: Any?): Nothing {
    throw ArgumentsExtractedException(listOf(*arguments))
}
