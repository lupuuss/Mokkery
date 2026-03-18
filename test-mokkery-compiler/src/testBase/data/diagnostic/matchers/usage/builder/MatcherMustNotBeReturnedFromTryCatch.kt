import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcherAllowed(): Int {
    return try {
        any<Int>()
        1
    } catch (e: Exception) {
        0
    }
}

fun MokkeryMatcherScope.matcherDenied(): Int {
    return <!ILLEGAL_TRY_CATCH!>try {
        any<Int>()
    } catch (e: Exception) {
        0
    }<!>
}
