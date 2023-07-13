package dev.mokkery.matcher.collections

import dev.mokkery.matcher.ArgMatcher

/**
 * Contains matchers for collections
 */
public object CollectionArgMatchers {

    public data class ValueInIterable<T>(val iterable: Iterable<T>): ArgMatcher<T> {
        override fun matches(arg: T): Boolean = arg in iterable
    }

    public data class ValueNotInIterable<T>(val iterable: Iterable<T>): ArgMatcher<T> {
        override fun matches(arg: T): Boolean = arg !in iterable
    }
}
