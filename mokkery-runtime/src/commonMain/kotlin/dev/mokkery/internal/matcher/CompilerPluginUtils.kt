@file:Suppress("unused")

package dev.mokkery.internal.matcher

import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.matcher.ArgMatcher

@PublishedApi
internal fun inlineLiteralsAsMatchers(array: Any): Array<ArgMatcher<Any?>> {
    val literals = array.asListOrNull()!!
    return Array(literals.size) { ArgMatcher.Equals(literals[it]) }
}
