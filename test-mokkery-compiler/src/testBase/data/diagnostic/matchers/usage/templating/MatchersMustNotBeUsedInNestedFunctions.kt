import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gte
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext

interface Foo {
    fun foo(arg: Int): Int
}

fun main() {
    val mock = mock<Foo>()
    every { mock.foo(1.let { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>gte(it)<!> }) }
    every {
        fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!>
        mock.foo(nested())
    }
    verify { mock.foo(1.let { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>gte(it)<!> }) }
    verify {
        fun nested() = <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!>
        mock.foo(nested())
    }
}

fun matcherVariableInLambda() {
    val mock = mock<Foo>()
    every {
        val matcher = any<Int>()
        mock.foo(1.let { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>matcher<!> })
    }
}

fun matcherInCtxValueLambda() {
    val mock = mock<Foo>()
    every { ctx({ <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!> }) { mock.foo(1) } }
}

fun scopeBlockInsideNestedLambda() {
    val mock = mock<Foo>()
    every { run { ctx(1) { mock.foo(<!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!>) } } }
    every { run { mock.ext { foo(<!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>any<Int>()<!>) } } }
}
