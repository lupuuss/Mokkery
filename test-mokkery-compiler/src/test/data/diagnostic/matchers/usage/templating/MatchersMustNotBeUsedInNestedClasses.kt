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
        class Bar {
            val nested = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
        }
        mock.foo(Bar().nested)
    }
    every {
        val bar = object {
            val nested = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
        }
        mock.foo(bar.nested)
    }
    verify {
        class Bar {
            val nested = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
        }
        mock.foo(Bar().nested)
    }
    verify {
        val bar = object {
            val nested = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
        }
        mock.foo(bar.nested)
    }
}
