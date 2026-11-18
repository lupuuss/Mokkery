import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any
import dev.mokkery.matcher.logical.not

interface Foo {
    fun foo(vararg args: Int): Int
}

fun main() {
    val mock = mock<Foo>()
    every {
        val matcher = any<IntArray>()
        mock.foo(1, *any(), <!SINGLE_VARARG_MATCHER_ALLOWED!>*matcher<!>, 10)
    }
    verify {
        val matcher = any<IntArray>()
        mock.foo(1, *any(), 10)
        mock.foo(1, *any(), <!SINGLE_VARARG_MATCHER_ALLOWED!>*matcher<!>, 10)
        mock.foo(1, *any(), <!SINGLE_VARARG_MATCHER_ALLOWED!>*matcher<!>, <!SINGLE_VARARG_MATCHER_ALLOWED!>*not(any())<!>, 10)
    }
}
