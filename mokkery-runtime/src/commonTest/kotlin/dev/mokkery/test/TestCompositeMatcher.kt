package dev.mokkery.test

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.propagateCapture

data class TestCompositeMatcher<T>(
    val takes: Int,
    val matchers: List<ArgMatcher<T>> = emptyList()
) : ArgMatcher.Composite<T> {

    var validated = false

    override fun matches(arg: T): Boolean = matchers.all { it.matches(arg) }

    override fun compose(matcher: ArgMatcher<T>): ArgMatcher.Composite<T> = copy(matchers = listOf(matcher) + matchers)

    override fun isFilled(): Boolean = takes == matchers.size

    override fun assertFilled() {
        validated = true
    }

    override fun capture(value: T) {
        matchers.propagateCapture(value)
    }

}
