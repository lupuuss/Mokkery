import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.verify
import dev.mokkery.verifySuspend

fun MokkeryMatcherScope.matcher(): Int {
    <!ILLEGAL_NESTED_TEMPLATING!>every { Unit }<!>
    <!ILLEGAL_NESTED_TEMPLATING!>everySuspend { }<!>
    <!ILLEGAL_NESTED_TEMPLATING!>verify { }<!>
    <!ILLEGAL_NESTED_TEMPLATING!>verifySuspend { }<!>
    return any<Int>()
}
