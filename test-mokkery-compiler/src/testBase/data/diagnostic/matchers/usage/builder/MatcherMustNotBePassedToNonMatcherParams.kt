import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gte

fun <T : Comparable<T>> MokkeryMatcherScope.matcher(): T = gte(<!MATCHER_PASSED_TO_NON_MATCHER_PARAM!>any()<!>)

fun MokkeryMatcherScope.regularParams(@Matcher matcher: Any?, value: Any?, vararg values: Any?): Any? = matcher

fun MokkeryMatcherScope.varargPassedToRegularParam(@Matcher vararg matchers: Any?): Any? {
    return regularParams(1, <!MATCHER_PASSED_TO_NON_MATCHER_PARAM!>matchers<!>)
}

fun MokkeryMatcherScope.varargSpreadToRegularVararg(@Matcher vararg matchers: Any?): Any? {
    return regularParams(1, 2, <!MATCHER_PASSED_TO_NON_MATCHER_PARAM!>*matchers<!>)
}

fun MokkeryMatcherScope.defaultFromMatcher(@Matcher matcher: Any?, value: Any? = <!MATCHER_PASSED_TO_NON_MATCHER_PARAM!>matcher<!>): Any? = matcher

fun MokkeryMatcherScope.matcherDefaultFromMatcher(@Matcher matcher: Any?, @Matcher other: Any? = matcher): Any? = other
