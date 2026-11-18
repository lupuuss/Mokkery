// LANGUAGE: +ContextParameters

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.any
import dev.mokkery.annotations.Matcher

context(<!PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER!>@Matcher<!> m: ArgMatcher<T>)
fun <T> MokkeryMatcherScope.matcherCtx(): T = any()

fun <T> <!PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER!>@receiver:Matcher<!> ArgMatcher<T>.matcherExt(scope: MokkeryMatcherScope): T = scope.any()

fun <T> MokkeryMatcherScope.matcherValue(<!PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER!>@Matcher<!> m: ArgMatcher<T>): T = any()
