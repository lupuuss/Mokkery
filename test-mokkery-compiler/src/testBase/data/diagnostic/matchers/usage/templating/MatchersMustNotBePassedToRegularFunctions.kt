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
        listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>matcher<!>)
        listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>any<Int>()<!>)
        <!ILLEGAL_MATCHER_IN_NON_MEMBER!>matcher<!>.toHexString()
        <!ILLEGAL_MATCHER_IN_NON_MEMBER!>any<Int>()<!>.toHexString()
        mock.foo(matcher)
    }
    verify {
        val matcher = any<Int>()
        listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>matcher<!>)
        listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>any<Int>()<!>)
        <!ILLEGAL_MATCHER_IN_NON_MEMBER!>matcher<!>.toHexString()
        <!ILLEGAL_MATCHER_IN_NON_MEMBER!>any<Int>()<!>.toHexString()
        mock.foo(matcher)
    }
}
