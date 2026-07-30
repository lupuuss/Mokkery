@file:Suppress("unused")

package dev.mokkery.internal.defaults

import dev.mokkery.internal.mokkeryRuntimeError

@PublishedApi
internal fun methodWithoutDefaultsError(): Nothing = mokkeryRuntimeError(
    "This method does not have defaults and should not be called!"
)

@PublishedApi
internal fun throwArguments(vararg arguments: Any?): Nothing {
    throw ArgumentsExtractedException(listOf(*arguments))
}
