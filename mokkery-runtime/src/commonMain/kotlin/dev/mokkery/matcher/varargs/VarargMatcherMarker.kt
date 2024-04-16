package dev.mokkery.matcher.varargs

import dev.mokkery.matcher.ArgMatcher

internal class VarargMatcherMarker(
    private val composite: ArgMatcher.Composite<Any?>
) : VarArgMatcher, ArgMatcher.Composite<Any?> by composite {

    override fun toString(): String = composite.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as VarargMatcherMarker
        return composite == other.composite
    }

    override fun hashCode(): Int = composite.hashCode()
}
