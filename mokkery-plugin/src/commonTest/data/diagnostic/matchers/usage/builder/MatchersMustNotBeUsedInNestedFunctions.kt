import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcherLambda(): Int {
    return 1.let { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>not(it)<!> }
}

fun MokkeryMatcherScope.matcherNestedFun(): Int {
    fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!>
    return nested()
}
