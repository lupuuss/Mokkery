import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any

interface Foo {
    fun foo(vararg args: Int): Int
}

fun main() {
    val mock = mock<Foo>()
    every {
        mock.foo(args = intArrayOf(1, *any(), 10))
        mock.foo(1, *intArrayOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION!>*any()<!>), 10)
    }
    verify {
        mock.foo(args = intArrayOf(*any(), 1, 10))
        mock.foo(1, *intArrayOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION!>*any()<!>), 10)
        mock.foo(1, *intArrayOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION!>*any()<!>), *any())
        mock.foo(args = intArrayOf(*intArrayOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION!>*any()<!>)))
    }
}
