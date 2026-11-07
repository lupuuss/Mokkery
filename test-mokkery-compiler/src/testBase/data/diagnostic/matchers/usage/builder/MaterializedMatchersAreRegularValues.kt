import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.propagateCapture
import dev.mokkery.matcher.matches
import dev.mokkery.matcher.materialize

@OptIn(DelicateMokkeryApi::class)
fun <T> MokkeryMatcherScope.logged(@Matcher first: T, @Matcher vararg rest: T): T {
    val matchers = listOf(materialize(first)) + materialize(*rest)
    val default = materialize(any<T>())
    println(matchers.joinToString())
    return matches(
        object : ArgMatcher.Composite<T> {
            override fun matches(arg: T): Boolean = matchers.all { it.matches(arg) } && default.matches(arg)

            override fun capture(value: T) = matchers.propagateCapture(value)
        }
    )
}

fun <T> MokkeryMatcherScope.matchesInsideLambda(@Matcher matcher: T): T {
    return matches { materialize(matcher).matches(it) }
}
