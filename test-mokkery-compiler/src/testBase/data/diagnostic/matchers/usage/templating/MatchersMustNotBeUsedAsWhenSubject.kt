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
        val matcher = any<Int>()
        when (<!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>matcher<!>) {
            else -> Unit
        }
        when (<!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>any<Int>()<!>) {
            else -> Unit
        }
        when (val m = <!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>matcher<!>) {
            else -> Unit
        }
        when (val m = <!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>any<Int>()<!>) {
            else -> Unit
        }
        mock.foo(any())
    }
    verify {
        val matcher = any<Int>()
        when (<!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>matcher<!>) {
            else -> Unit
        }
        when (<!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>any<Int>()<!>) {
            else -> Unit
        }
        when (val m = <!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>matcher<!>) {
            else -> Unit
        }
        when (val m = <!ILLEGAL_MATCHER_IN_WHEN_SUBJECT!>any<Int>()<!>) {
            else -> Unit
        }
        mock.foo(any())
    }
}
