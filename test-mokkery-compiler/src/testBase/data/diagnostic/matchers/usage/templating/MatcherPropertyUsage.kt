import dev.mokkery.annotations.Matcher
import dev.mokkery.every
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.logical.not
import dev.mokkery.mock

interface Foo {
    fun foo(arg: Int): Int
}

val MokkeryMatcherScope.gtOne: Int
    get() = gt(1)

fun test(mock: Foo) {
    every { mock.foo(gtOne) }
    every { mock.foo(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>gtOne<!> + 1) }
    every { mock.foo(listOf(1).map { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>gtOne<!> }.size) }
    every { mock.foo(maxOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>gtOne<!>, 1)) }
}

context(scope: MokkeryMatcherScope)
val @receiver:Matcher Int.negated: Int
    get() = scope.not(this)

fun testPropertyParameters(mock: Foo) {
    every { mock.foo(gt(1).negated) }
    every { mock.foo(1.negated) }
}
