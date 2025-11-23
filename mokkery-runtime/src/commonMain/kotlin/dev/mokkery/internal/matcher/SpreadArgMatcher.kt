package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher

internal interface SpreadArgMatcher<T> : ArgMatcher<T>

internal fun <T> ArgMatcher<T>.spread(): SpreadArgMatcher<T> = SpreadArgMatcherImpl(this)

private class SpreadArgMatcherImpl<T>(private val matcher: ArgMatcher<T>) : SpreadArgMatcher<T> {

    override fun matches(arg: T): Boolean = matcher.matches(arg)

    override fun toString(): String = "*$matcher"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SpreadArgMatcherImpl<T>
        return matcher == other.matcher
    }

    override fun hashCode(): Int = matcher.hashCode()
}
