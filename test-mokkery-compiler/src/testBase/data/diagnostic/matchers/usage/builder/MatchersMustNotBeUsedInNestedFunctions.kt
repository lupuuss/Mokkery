import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcherLambda(): Int {
    return 1.let { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>not(it)<!> }
}

fun MokkeryMatcherScope.matcherNestedFun(): Int {
    fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!>
    return nested()
}

fun <T> MokkeryMatcherScope.matcherParamInLambda(@Matcher matcher: T): T = run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>matcher<!> }

fun <T> MokkeryMatcherScope.matcherVariableInLambda(@Matcher matcher: T): T {
    val copy = matcher
    return run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>copy<!> }
}

fun <T> MokkeryMatcherScope.matcherParamPassedToMatcherInLambda(@Matcher matcher: T): T = run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>not(<!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>matcher<!>)<!> }

fun <T> MokkeryMatcherScope.varargMatcherInLambda(@Matcher first: T, @Matcher vararg matchers: T): T {
    return or(first, first, *run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>matchers<!> })
}

fun <T> MokkeryMatcherScope.matcherParamInNestedFun(@Matcher matcher: T): T {
    fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>matcher<!>
    return nested()
}

fun MokkeryMatcherScope.otherErrorsReportedInLambda(@Matcher matcher: Int): Int {
    run { println(<!ILLEGAL_MATCHER_IN_NON_MEMBER, ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>matcher<!>) }
    return matcher
}
