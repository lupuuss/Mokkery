package dev.mokkery.matcher.collections

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentDeepEqualsArgMatcherTest {

    private val matcher = CollectionArgMatchers.ContentDeepEquals(
        arrayOf(
            arrayOf(1),
            arrayOf(2)
        )
    )

    @Test
    fun testDoesNotMatchArrayWithDifferentDeepContent() {
        assertFalse(matcher.matches(arrayOf(arrayOf(1), arrayOf(1))))
        assertFalse(matcher.matches(arrayOf(arrayOf(2), arrayOf(1))))
    }

    @Test
    fun testMatchesArrayWithTheSameDeepContent() {
        val array = arrayOf(
            arrayOf(1),
            arrayOf(2)
        )
        assertTrue(matcher.matches(array))
    }
}
