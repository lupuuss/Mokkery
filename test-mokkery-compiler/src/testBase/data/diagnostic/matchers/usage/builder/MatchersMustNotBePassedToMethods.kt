import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.mock

interface Foo {
    var prop: Int

    fun foo(i: Int)
}

val mock = mock<Foo>()

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    mock.foo(<!MATCHER_PASSED_TO_MEMBER_IN_MATCHER_BUILDER!>matcher<!>)
    mock.prop = <!MATCHER_PASSED_TO_MEMBER_IN_MATCHER_BUILDER!>matcher<!>
    return matcher
}

class Holder {
    val Int.memberExtProp: Int get() = this

    fun MokkeryMatcherScope.memberMatcher(): Int {
        val matcher = any<Int>()
        println(<!MATCHER_PASSED_TO_MEMBER_IN_MATCHER_BUILDER!>matcher<!>.memberExtProp)
        return matcher
    }
}
