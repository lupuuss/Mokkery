import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any

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
