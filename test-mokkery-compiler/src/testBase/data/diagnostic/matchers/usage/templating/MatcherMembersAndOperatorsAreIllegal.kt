import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.materialize
import dev.mokkery.matcher.logical.or
import dev.mokkery.templating.ext

interface Foo {
    fun foo(arg: Int): Int
    fun bar(arg: Int?): Int?
    fun baz(arg: String): Int
    val callback: (Int) -> Int
}

fun main() {
    val mock = mock<Foo>()
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<String>()<!>.length) }
    every { mock.bar(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<String?>()<!>?.length) }
    every { mock.foo(<!ILLEGAL_OPERATOR_USAGE!>any<Int?>()<!>!!) }
    every { mock.foo(<!ILLEGAL_OPERATOR_USAGE!>any<Int?>()<!> ?: 1) }
    every { mock.baz("value: ${<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<Int>()<!>}") }
    every {
        val (<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>first<!>, <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>second<!>) = any<Pair<Int, Int>>()
        mock.foo(first + second)
    }
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<() -> Int>()<!>()) }
    every { mock.foo(<!ILLEGAL_OPERATOR_USAGE!>any<String>()<!>::length.get()) }
    every { mock.callback(any()) }
    every { any<Foo>().ext { <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>foo(1)<!> } }
    every { mock.foo(or(1, 2, *listOf(3, 4).map(<!ILLEGAL_OPERATOR_USAGE!>::gt<!>).toTypedArray())) }
    every { mock.foo(listOf(3, 4).map(<!ILLEGAL_OPERATOR_USAGE!>this::materialize<!>).size) }
}
