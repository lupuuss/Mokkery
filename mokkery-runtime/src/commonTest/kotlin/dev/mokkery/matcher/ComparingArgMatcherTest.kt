package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComparingArgMatcherTest {

    private fun matcher(type: ArgMatcher.Comparing.Type) = ArgMatcher.Comparing(4, type)

    @Test
    fun testReturnsCorrectBooleanForSpecificType() {
        val cases = listOf(
            TestCase(
                type = ArgMatcher.Comparing.Type.Eq,
                shouldReturnTrue = listOf(4),
                shouldReturnFalse = listOf(1, 2, 3, 5, 6, 7, 8, 9, 10)
            ),
            TestCase(
                type = ArgMatcher.Comparing.Type.Gt,
                shouldReturnTrue = listOf(5, 6, 7, 8, 9, 10),
                shouldReturnFalse = listOf(1, 2, 3, 4)
            ),
            TestCase(
                type = ArgMatcher.Comparing.Type.Gte,
                shouldReturnTrue = listOf(4, 5, 6, 7, 8, 9, 10),
                shouldReturnFalse = listOf(1, 2, 3)
            ),
            TestCase(
                type = ArgMatcher.Comparing.Type.Lt,
                shouldReturnTrue = listOf(1, 2, 3),
                shouldReturnFalse = listOf(4, 5, 6, 7, 8, 9, 10)
            ),
            TestCase(
                type = ArgMatcher.Comparing.Type.Lte,
                shouldReturnTrue = listOf(1, 2, 3, 4),
                shouldReturnFalse = listOf(5, 6, 7, 8, 9, 10)
            )
        )
        cases.forEach { case ->
            case.shouldReturnTrue.forEach {
                assertTrue(
                    matcher(case.type).matches(it),
                    "Matcher for type ${case.type} should return true for $it!"
                )
            }
            case.shouldReturnFalse.forEach {
                assertFalse(
                    matcher(case.type).matches(it),
                    "Matcher for type ${case.type} should return false for $it!"
                )
            }
        }
    }

    private data class TestCase(
        val type: ArgMatcher.Comparing.Type,
        val shouldReturnTrue: List<Int>,
        val shouldReturnFalse: List<Int>,
    )
}
