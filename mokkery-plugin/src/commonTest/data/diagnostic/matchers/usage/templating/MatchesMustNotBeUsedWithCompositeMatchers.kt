import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.matches

interface Foo {
    fun foo(arg: Int): Int
}

@OptIn(dev.mokkery.annotations.DelicateMokkeryApi::class)
class FooCompositeMatcher : ArgMatcher.Composite<Int> {

    override fun matches(arg: Int): Boolean = true

    override fun capture(value: Int): Unit = Unit
}

fun main() {
    val mock = mock<Foo>()
    every { mock.foo(matches(<!MATCHES_WITH_COMPOSITE_ARG!>FooCompositeMatcher()<!>)) }
    verify { mock.foo(matches(<!MATCHES_WITH_COMPOSITE_ARG!>FooCompositeMatcher()<!>)) }
}
