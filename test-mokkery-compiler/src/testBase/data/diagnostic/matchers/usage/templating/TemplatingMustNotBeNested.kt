import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext

interface Foo {
    fun foo(arg: Int): Int
}

fun main() {
    val mock = mock<Foo>()
    every {
        <!ILLEGAL_NESTED_TEMPLATING!>every { }<!>
        <!ILLEGAL_NESTED_TEMPLATING!>verify { }<!>
        mock.foo(any<Int>())
    }
    verify {
        <!ILLEGAL_NESTED_TEMPLATING!>every { }<!>
        <!ILLEGAL_NESTED_TEMPLATING!>verify { }<!>
        mock.foo(any<Int>())
    }
}

fun contextFunctionsInLocalBuilder() {
    val mock = mock<Foo>()
    every {
        fun MokkeryMatcherScope.local(@Matcher x: Int): Int {
            <!ILLEGAL_CONTEXT_FUNCTION_IN_MATCHER_BUILDER!>ctx(1) { }<!>
            <!ILLEGAL_CONTEXT_FUNCTION_IN_MATCHER_BUILDER!>1.ext { }<!>
            return x
        }
        mock.foo(local(any()))
    }
}
