import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any

interface Foo {
    fun foo(arg: Int): Int
}

fun mainIf() {
    val mock = mock<Foo>()
    every {
        val matcher = any<Boolean>()
        if (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>) Unit
        if (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>) Unit
        mock.foo(any())
    }
    verify {
        val matcher = any<Boolean>()
        if (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>) Unit
        if (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>) Unit
        mock.foo(any())
    }
}

fun mainWhen() {
    val mock = mock<Foo>()
    every {
        val matcher = any<Boolean>()
        when {
            <!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!> -> Unit
            <!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!> -> Unit
            else -> Unit
        }
        mock.foo(any())
    }
    verify {
        val matcher = any<Boolean>()
        when {
            <!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!> -> Unit
            <!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!> -> Unit
            else -> Unit
        }
        mock.foo(any())
    }
}

fun mainLoopCondition() {
    val mock = mock<Foo>()
    every {
        val matcher = any<Boolean>()
        while (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>) Unit
        while (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>) Unit
        do { } while (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>)
        do { } while (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>)
        mock.foo(any())
    }
    verify {
        val matcher = any<Boolean>()
        while (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>) Unit
        while (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>) Unit
        do { } while (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>)
        do { } while (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>)
        mock.foo(any())
    }
}
