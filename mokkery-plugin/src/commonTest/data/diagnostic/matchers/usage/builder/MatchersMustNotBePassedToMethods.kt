import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.mock

interface Foo {
    fun foo(i: Int)
}

val mock = mock<Foo>()

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    mock.foo(<!MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER!>matcher<!>)
    return matcher
}
