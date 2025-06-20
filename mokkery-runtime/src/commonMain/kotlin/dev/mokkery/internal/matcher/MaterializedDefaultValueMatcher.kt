package dev.mokkery.internal.matcher

import dev.mokkery.internal.utils.description
import dev.mokkery.matcher.ArgMatcher

internal data class MaterializedDefaultValueMatcher<T>(val defaultValue: T) : ArgMatcher<T> {

    override fun matches(arg: T): Boolean = arg == defaultValue

    override fun toString(): String = "default() => ${defaultValue.description()}"
}
