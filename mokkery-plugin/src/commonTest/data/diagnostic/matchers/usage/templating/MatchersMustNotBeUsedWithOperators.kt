import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any

interface Foo {
    fun foo(arg: String): Int
}

fun main() {
    val mock = mock<Foo>()
    every {
        val matcher = any<String>()
        <!ILLEGAL_OPERATOR_USAGE!>any<String>()<!>::class
        <!ILLEGAL_OPERATOR_USAGE!>matcher<!>::class
        <!ILLEGAL_OPERATOR_USAGE!>any<String>()<!> == ""
        <!ILLEGAL_OPERATOR_USAGE!>matcher<!> == ""
        <!ILLEGAL_OPERATOR_USAGE!>any<String>()<!> === ""
        <!ILLEGAL_OPERATOR_USAGE!>matcher<!> === ""
        mock.foo(matcher)
    }
    verify {
        val matcher = any<String>()
        <!ILLEGAL_OPERATOR_USAGE!>any<String>()<!>::class
        <!ILLEGAL_OPERATOR_USAGE!>matcher<!>::class
        <!ILLEGAL_OPERATOR_USAGE!>any<String>()<!> == ""
        <!ILLEGAL_OPERATOR_USAGE!>matcher<!> == ""
        <!ILLEGAL_OPERATOR_USAGE!>any<String>()<!> === ""
        <!ILLEGAL_OPERATOR_USAGE!>matcher<!> === ""
        mock.foo(matcher)
    }
}
