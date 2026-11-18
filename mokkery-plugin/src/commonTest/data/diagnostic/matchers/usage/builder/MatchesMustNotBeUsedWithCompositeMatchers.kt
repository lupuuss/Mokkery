import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches
import dev.mokkery.matcher.ArgMatcher

@OptIn(dev.mokkery.annotations.DelicateMokkeryApi::class)
class FooCompositeMatcher : ArgMatcher.Composite<Int> {

    override fun matches(arg: Int): Boolean = true

    override fun capture(value: Int): Unit = Unit
}

fun MokkeryMatcherScope.matcher(): Int = matches(<!MATCHES_WITH_COMPOSITE_ARG!>FooCompositeMatcher()<!>)
