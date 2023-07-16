package dev.mokkery.matcher.collections

import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentEqualsArgMatcherTest {

    @Test
    fun testFailsIfConstructParameterIsNotAnArray() {
        assertFails {
            CollectionArgMatchers.ContentEquals(1)
        }
    }

    @Test
    fun testDoesNotMatchWhenArgIsNotAnArray() {
        assertFalse(CollectionArgMatchers.ContentEquals(intArrayOf(1)).matches(1))
    }

    @Test
    fun testDoesNotMatchWhenArgIsArrayOfDifferentType() {
        assertFalse(CollectionArgMatchers.ContentEquals(intArrayOf(1)).matches(longArrayOf(1)))
    }

    @Test
    fun testDoesNotMatchWhenArgIsArrayOfDifferentContent() {
        assertFalse(CollectionArgMatchers.ContentEquals(intArrayOf(1)).matches(intArrayOf(1, 1)))
    }

    @Test
    fun testMatchesWhenArgIsArrayWithTheSameTypeAndContent() {
        assertTrue(CollectionArgMatchers.ContentEquals(intArrayOf(1, 2)).matches(intArrayOf(1, 2)))
    }
}
