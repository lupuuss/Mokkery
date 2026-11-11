import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.materialize

fun MokkeryMatcherScope.localMatcher(@Matcher value: Int): Int = value

val MokkeryMatcherScope.matcherProperty: Int
    get() = gt(1)

val topLevelReference: MokkeryMatcherScope.(Int) -> Int = <!ILLEGAL_MATCHER_REFERENCE!>MokkeryMatcherScope::localMatcher<!>

fun regularFunction() {
    val local: MokkeryMatcherScope.(Int) -> Int = <!ILLEGAL_MATCHER_REFERENCE!>MokkeryMatcherScope::localMatcher<!>
    val external: MokkeryMatcherScope.(Int) -> Int = <!ILLEGAL_MATCHER_REFERENCE!>MokkeryMatcherScope::gt<!>
    val materializer: MokkeryMatcherScope.(Int) -> ArgMatcher<Int> = <!ILLEGAL_MATCHER_REFERENCE!>MokkeryMatcherScope::materialize<!>
    val property = <!ILLEGAL_MATCHER_REFERENCE!>MokkeryMatcherScope::matcherProperty<!>
}
