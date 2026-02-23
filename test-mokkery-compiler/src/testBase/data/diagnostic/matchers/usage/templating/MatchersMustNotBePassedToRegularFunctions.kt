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
        listOf(<!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>matcher<!>)
        listOf(<!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>any<Int>()<!>)
        <!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>matcher<!>.toHexString()
        <!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>any<Int>()<!>.toHexString()
        mock.foo(matcher)
    }
    verify {
        val matcher = any<Int>()
        listOf(<!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>matcher<!>)
        listOf(<!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>any<Int>()<!>)
        <!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>matcher<!>.toHexString()
        <!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>any<Int>()<!>.toHexString()
        mock.foo(matcher)
    }
}
