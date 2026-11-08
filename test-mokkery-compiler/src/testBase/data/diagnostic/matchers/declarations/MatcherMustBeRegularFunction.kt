import dev.mokkery.matcher.MokkeryMatcherScope

val matcher: MokkeryMatcherScope.() -> Int = <!MATCHER_MUST_BE_REGULAR_FUNCTION!>{ 1 }<!>

<!MATCHER_MUST_BE_REGULAR_FUNCTION!>suspend fun MokkeryMatcherScope.suspendMatcher(): Int = 1<!>
