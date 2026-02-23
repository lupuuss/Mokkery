import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gte

interface Foo {
    fun foo(arg: Int): Int
}

fun main() {
    val mock = mock<Foo>()
    every { mock.foo(1.let { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>gte(it)<!> }) }
    every {
        fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!>
        mock.foo(nested())
    }
    verify { mock.foo(1.let { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>gte(it)<!> }) }
    verify {
        fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!>
        mock.foo(nested())
    }
}
