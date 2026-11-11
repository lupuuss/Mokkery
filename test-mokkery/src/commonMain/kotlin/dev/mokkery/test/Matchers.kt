package dev.mokkery.test

import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.matches

fun <T> MokkeryMatcherScope.externalMatcher(value: T): T = matches { it == value }

open class GenericMatcherBuilders<T> {
    fun MokkeryMatcherScope.inheritedOr(@Matcher first: T, @Matcher second: T): T = or(first, second)
}

class IntMatcherBuilders : GenericMatcherBuilders<Int>()

open class PlainMatcherBuilders {
    fun MokkeryMatcherScope.inheritedOr(@Matcher first: Int, @Matcher second: Int): Int = or(first, second)
}
