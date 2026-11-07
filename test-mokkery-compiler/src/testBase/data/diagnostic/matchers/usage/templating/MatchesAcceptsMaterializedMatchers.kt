import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.any
import dev.mokkery.matcher.logical.LogicalMatchers
import dev.mokkery.matcher.matches
import dev.mokkery.matcher.materialize
import dev.mokkery.mock

interface Foo {

    fun call(a: Int, b: Double): String
}

@OptIn(DelicateMokkeryApi::class)
fun test() {
    val mock = mock<Foo>()
    every {
        val first = materialize(any<Int>())
        first.matches(1)
        mock.call(
            matches(LogicalMatchers.Not(listOf(first))),
            matches(ArgMatcher.Equals(2.0)),
        )
    } returns ""
}
