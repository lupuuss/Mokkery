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
        <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!>.toString()
        <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<Int>()<!>.toString()
        <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!> > 1
        <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<Int>()<!> > 1
        mock.foo(matcher)
    }
    verify {
        val matcher = any<Int>()
        <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!>.toString()
        <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<Int>()<!>.toString()
        <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!> > 1
        <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<Int>()<!> > 1
        mock.foo(matcher)
    }
}
