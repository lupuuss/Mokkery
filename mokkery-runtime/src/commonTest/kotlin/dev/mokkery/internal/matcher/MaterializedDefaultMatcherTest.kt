package dev.mokkery.internal.matcher

import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.testRendering
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaterializedDefaultMatcherTest {

    private val matcher = MaterializedDefaultValueMatcher(23)

    @Test
    fun testReturnsTrueWhenEqual() {
        assertTrue(matcher.matches(23))
    }

    @Test
    fun testReturnsFalseWhenNotEqual() {
        assertFalse(matcher.matches(22))
    }

    @Test
    fun testRenderReturnsExpectedValue() {
        val descriptionRenderer = TestRenderer<Any?>(MokkeryRendering.descriptionKey) { "DESCRIPTION($it)" }
        assertEquals(
            expected = "default() => DESCRIPTION(23)",
            actual = testRendering(descriptionRenderer) { matcher.render() }
        )
    }
}
