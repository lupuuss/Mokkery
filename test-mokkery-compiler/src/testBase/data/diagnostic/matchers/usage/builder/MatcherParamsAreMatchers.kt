import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun <T> MokkeryMatcherScope.compared(@Matcher matcher: T): T {
    if (<!ILLEGAL_OPERATOR_USAGE!>matcher<!> == null) return any()
    return matcher
}

fun <T> MokkeryMatcherScope.printed(@Matcher matcher: T): T {
    println(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>matcher<!>)
    return matcher
}
