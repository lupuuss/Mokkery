import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun <T> MokkeryMatcherScope.compared(@Matcher matcher: T): T {
    if (<!ILLEGAL_OPERATOR_USAGE!>matcher<!> == null) return any()
    return matcher
}

fun <T> MokkeryMatcherScope.printed(@Matcher matcher: T): T {
    println(<!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>matcher<!>)
    return matcher
}
