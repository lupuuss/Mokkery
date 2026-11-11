@file:Suppress("unused", "UnusedReceiverParameter")

package dev.mokkery.internal.matcher

import dev.mokkery.annotations.Matcher
import dev.mokkery.internal.mokkeryIntrinsic
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches
import dev.mokkery.matcher.materialize

@PublishedApi
internal fun <T> internalMaterialize(vararg matchers: ArgMatcher<T>): List<ArgMatcher<T>> = matchers.flatMap { matcher ->
    val literals = (matcher as? SpreadArgMatcher<*>)?.matcher as? ArgMatcher.Equals<*>
    literals?.value.asListOrNull()?.map { ArgMatcher.Equals(it).unsafeCast() } ?: listOf(matcher)
}

@PublishedApi
internal fun <T> MokkeryMatcherScope.eq(value: T): T = matches(ArgMatcher.Equals(value))

@PublishedApi
internal fun <T> MokkeryMatcherScope.spread(
    @Matcher arrayMatcher: Any?
): T = matches(SpreadArgMatcher(materialize(arrayMatcher)))

@PublishedApi
internal fun <A> MokkeryMatcherScope.spreadArray(@Matcher value: A): A = mokkeryIntrinsic

@PublishedApi
internal fun <T> internalSpreadArray(
    matcher: ArgMatcher<*>
): Array<ArgMatcher<T>> = arrayOf(SpreadArgMatcher(matcher.unsafeCast()))

@PublishedApi
internal fun <T> MokkeryMatcherScope.varArgs(
    @Matcher vararg elements: Any?
): T = matches(CompositeVarArgMatcher(materialize(*elements)))

@PublishedApi
internal fun <T> MokkeryMatcherScope.defaultValue(
    mask: Long,
    extractingFunction: Function<Nothing>,
    isExtractingFunctionSuspend: Boolean,
): T = matches(DefaultValuesMatcher(mask, extractingFunction, isExtractingFunctionSuspend))
