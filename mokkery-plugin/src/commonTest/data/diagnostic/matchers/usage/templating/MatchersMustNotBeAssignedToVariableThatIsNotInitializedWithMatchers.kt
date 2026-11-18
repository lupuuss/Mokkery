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
        var foo: Int = 0
        <!VARIABLE_NOT_A_MATCHER!>foo = any<Int>()<!>
        mock.foo(foo)
    }
    verify {
        var foo: Int = 0
        <!VARIABLE_NOT_A_MATCHER!>foo = any<Int>()<!>
        mock.foo(foo)
    }
}
