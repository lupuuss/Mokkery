import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcherLambda(): Int {
    return 1.let { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>not(it)<!> }
}

fun MokkeryMatcherScope.matcherNestedFun(): Int {
    fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>any<Int>()<!>
    return nested()
}

fun <T> MokkeryMatcherScope.matcherParamInLambda(@Matcher matcher: T): T = run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>matcher<!> }

fun <T> MokkeryMatcherScope.matcherVariableInLambda(@Matcher matcher: T): T {
    val copy = matcher
    return run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>copy<!> }
}

fun <T> MokkeryMatcherScope.matcherParamPassedToMatcherInLambda(@Matcher matcher: T): T = run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>not(<!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>matcher<!>)<!> }

fun <T> MokkeryMatcherScope.varargMatcherInLambda(@Matcher first: T, @Matcher vararg matchers: T): T {
    return or(first, first, *run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>matchers<!> })
}

fun <T> MokkeryMatcherScope.matcherParamInNestedFun(@Matcher matcher: T): T {
    fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>matcher<!>
    return nested()
}

fun MokkeryMatcherScope.otherErrorsReportedInLambda(@Matcher matcher: Int): Int {
    run { println(<!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER, MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>matcher<!>) }
    return matcher
}

fun MokkeryMatcherScope.localBuilderComposingItsParam(): Int {
    fun MokkeryMatcherScope.local(@Matcher x: Int): Int = not(x)
    return local(any())
}

fun MokkeryMatcherScope.localBuilderCapturingMatcherParam(@Matcher matcher: Int): Int {
    fun MokkeryMatcherScope.local(): Int = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>matcher<!>
    fun MokkeryMatcherScope.composing(): Int = not(<!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>matcher<!>)
    fun MokkeryMatcherScope.viaVariable(): Int {
        val copy = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>matcher<!>
        return copy
    }
    return or(local(), composing(), viaVariable())
}
