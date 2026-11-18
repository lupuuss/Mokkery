import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any

interface Foo {
    fun foo(arg: Int): Int
}

var foo: Int = 0

fun main() {
    val mock = mock<Foo>()
    every {
        <!VARIABLE_OUT_OF_SCOPE!>foo = any<Int>()<!>
        mock.foo(foo)
    }
    verify {
        <!VARIABLE_OUT_OF_SCOPE!>foo = any<Int>()<!>
        mock.foo(foo)
    }
}
