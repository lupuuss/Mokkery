import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or

fun MokkeryMatcherScope.passedToSingleParam(@Matcher vararg matchers: Any?): Any? = not(<!VARARG_MATCHER_WITHOUT_SPREAD!>matchers<!>)

fun MokkeryMatcherScope.passedAsVarargElement(@Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    return or(first, first, <!VARARG_MATCHER_WITHOUT_SPREAD!>matchers<!>)
}

fun MokkeryMatcherScope.passedViaVariable(@Matcher vararg matchers: Any?): Any? {
    val copy = matchers
    return not(<!VARARG_MATCHER_WITHOUT_SPREAD!>copy<!>)
}

fun <T> MokkeryMatcherScope.passedWithSpread(@Matcher first: T, @Matcher vararg rest: T): T {
    val copy = rest
    or(first, first, *copy)
    return or(first, first, *rest)
}
