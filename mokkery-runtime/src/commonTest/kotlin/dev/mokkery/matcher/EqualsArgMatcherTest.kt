package dev.mokkery.matcher

import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.testRendering
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EqualsArgMatcherTest {

    private val matcher = ArgMatcher.Equals(1)

    @Test
    fun testReturnsTrueForSpecifiedValue() {
        assertTrue(matcher.matches(1))
    }

    @Test
    fun testReturnsFalseForOtherValues() {
        assertFalse(matcher.matches(0))
        assertFalse(matcher.matches(100))
        assertFalse(matcher.matches(-100))
    }

    @Test
    fun testRenderReturnsCorrectDescription() {
        val testRenderer = TestRenderer<Any?>(MokkeryRendering.descriptionKey) { "DESCRIPTION($it)" }
        assertEquals("DESCRIPTION(1)", testRendering(testRenderer) { matcher.render() })
    }
}
