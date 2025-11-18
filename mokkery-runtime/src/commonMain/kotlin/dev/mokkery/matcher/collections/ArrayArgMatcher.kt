package dev.mokkery.matcher.collections

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.matcher.ArgMatcher

/**
 * Base class for creating matchers for arrays.
 * It allows creating single implementation for all array types using [matchesElements].
 *
 * Subclass should not be used directly and wrapped in a type-safe manner.
 * Check existing subclasses and their usage for guidance e.g.
 * [CollectionArgMatchers.ContainsAllArray] and it's usage in [containsAll], [containsAllInts], [containsAllElements] etc.
 */
@DelicateMokkeryApi
public abstract class ArrayArgMatcher<T> : ArgMatcher<Any?> {

    final override fun matches(arg: Any?): Boolean {
        val varargs = arg.asListOrNull() ?: return false
        return matchesElements(varargs.unsafeCast())
    }

    public abstract fun matchesElements(elements: List<T>): Boolean
}
