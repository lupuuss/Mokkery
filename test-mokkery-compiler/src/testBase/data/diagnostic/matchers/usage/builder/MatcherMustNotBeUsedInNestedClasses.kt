import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcherClass(): Int {
    class Foo {
        fun nested() = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
    }
    return Foo().nested()
}

fun MokkeryMatcherScope.matcherObject(): Int {
    val obj = object {
        fun nested() = <!ILLEGAL_NESTED_CLASS_MATCHERS!>any<Int>()<!>
    }
    return obj.nested()
}
