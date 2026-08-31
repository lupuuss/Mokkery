import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.logical.LogicalMatchers
import dev.mokkery.matcher.matches
import dev.mokkery.mock

interface Foo {

    fun call(a: Int, b: Double): String
}

@OptIn(DelicateMokkeryApi::class)
fun test() {
    val mock = mock<Foo>()
    // a composite argument must not make the following regular ones report as composite
    every {
        mock.call(
            matches(<!MATCHES_WITH_COMPOSITE_ARG!>LogicalMatchers.Not(listOf(ArgMatcher.Equals(1)))<!>),
            matches(ArgMatcher.Equals(2.0)),
        )
    } returns ""
    // a regular argument must not hide a composite one that follows
    every {
        mock.call(
            matches(ArgMatcher.Equals(1)),
            matches(<!MATCHES_WITH_COMPOSITE_ARG!>LogicalMatchers.Not(listOf(ArgMatcher.Equals(2.0)))<!>),
        )
    } returns ""
}
