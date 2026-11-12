import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.logical.not
import dev.mokkery.templating.ext

interface Foo {
    fun foo(arg: Int): Int
}

fun main() {
    val mock = mock<Foo>()
    every {
        class Bar {
            val nested = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
        }
        mock.foo(Bar().nested)
    }
    every {
        val bar = object {
            val nested = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
        }
        mock.foo(bar.nested)
    }
    verify {
        class Bar {
            val nested = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
        }
        mock.foo(Bar().nested)
    }
    verify {
        val bar = object {
            val nested = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
        }
        mock.foo(bar.nested)
    }
}

fun matcherBuildersInLocalClasses() {
    val mock = mock<Foo>()
    every {
        val builders = object {
            fun MokkeryMatcherScope.negated(@Matcher x: Int): Int = not(x)
        }
        builders.ext { mock.foo(negated(any())) }
    }
    every {
        val matcher = any<Int>()
        val builders = object {
            fun MokkeryMatcherScope.captured(): Int = <!ILLEGAL_NESTED_CLASS_MATCHERS!>matcher<!>
        }
        builders.ext { mock.foo(captured()) }
    }
}
