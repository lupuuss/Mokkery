package dev.mokkery.internal.verify.results

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateMatchingResultsComposerTest {

    private val callMatcher = TestCallMatcher { trace, template ->
        if (trace.name == template.name) CallMatchResult.Matching else CallMatchResult.NotMatching
    }
    private val builder = TemplateMatchingResultsComposer(callMatcher)

    @Test
    fun testMapsTemplateToNoMatchWhenNoOtherCalls() {
        val result = builder.compose(
            traces = listOf(),
            templates = listOf(fakeCallTemplate(name = "a"), fakeCallTemplate(name = "c"))
        )
        val expectedResult = listOf(
            TemplateMatchingResult.NoMatch(fakeCallTemplate(name = "a")),
            TemplateMatchingResult.NoMatch(fakeCallTemplate(name = "c")),
        )
        assertEquals(expectedResult, result)
    }

    @Test
    fun testMapsMatchingTemplate() {
        val result = builder.compose(
            traces = listOf(fakeCallTrace(name = "a"), fakeCallTrace(name = "b")),
            templates = listOf(fakeCallTemplate(name = "a"), fakeCallTemplate(name = "b"))
        )
        val expectedResult = listOf(
            TemplateMatchingResult.Matching(fakeCallTrace(name = "a"), fakeCallTemplate(name = "a")),
            TemplateMatchingResult.Matching(fakeCallTrace(name = "b"), fakeCallTemplate(name = "b")),
        )
        assertEquals(expectedResult, result)
    }

    @Test
    fun testMapsUnverifiedCallsWhenNoTemplates() {
        val result = builder.compose(
            traces = listOf(fakeCallTrace(name = "a"), fakeCallTrace(name = "b")),
            templates = emptyList()
        )
        val expectedResult = listOf(
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "a")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "b")),
        )
        assertEquals(expectedResult, result)
    }

    @Test
    fun testMapsCallsBetweenMatchesAsUnverified() {
        val result = builder.compose(
            traces = listOf(
                fakeCallTrace(name = "a"),
                fakeCallTrace(name = "b"),
                fakeCallTrace(name = "c"),
                fakeCallTrace(name = "d"),
            ),
            templates = listOf(
                fakeCallTemplate(name = "a"),
                fakeCallTemplate(name = "d")
            )
        )
        val expectedResult = listOf(
            TemplateMatchingResult.Matching(fakeCallTrace(name = "a"), fakeCallTemplate(name = "a")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "b")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "c")),
            TemplateMatchingResult.Matching(fakeCallTrace(name = "d"), fakeCallTemplate(name = "d")),
            )
        assertEquals(expectedResult, result)
    }

    @Test
    fun testMapsCallsBeforeMatchesAsUnverified() {
        val result = builder.compose(
            traces = listOf(
                fakeCallTrace(name = "a"),
                fakeCallTrace(name = "b"),
                fakeCallTrace(name = "c"),
                fakeCallTrace(name = "d"),
            ),
            templates = listOf(
                fakeCallTemplate(name = "c"),
                fakeCallTemplate(name = "d")
            )
        )
        val expectedResult = listOf(
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "a")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "b")),
            TemplateMatchingResult.Matching(fakeCallTrace(name = "c"), fakeCallTemplate(name = "c")),
            TemplateMatchingResult.Matching(fakeCallTrace(name = "d"), fakeCallTemplate(name = "d")),
        )
        assertEquals(expectedResult, result)
    }

    @Test
    fun testMapsCallsAfterMatchesAsUnverified() {
        val result = builder.compose(
            traces = listOf(
                fakeCallTrace(name = "a"),
                fakeCallTrace(name = "b"),
                fakeCallTrace(name = "c"),
                fakeCallTrace(name = "d"),
            ),
            templates = listOf(
                fakeCallTemplate(name = "a"),
                fakeCallTemplate(name = "b")
            )
        )
        val expectedResult = listOf(
            TemplateMatchingResult.Matching(fakeCallTrace(name = "a"), fakeCallTemplate(name = "a")),
            TemplateMatchingResult.Matching(fakeCallTrace(name = "b"), fakeCallTemplate(name = "b")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "c")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "d")),
        )
        assertEquals(expectedResult, result)
    }

    @Test
    fun testMixesAllTypesOfResultsInCorrectOrder() {
        val result = builder.compose(
            traces = listOf(
                fakeCallTrace(name = "a"),
                fakeCallTrace(name = "a"),
                fakeCallTrace(name = "b"),
                fakeCallTrace(name = "b"),
                fakeCallTrace(name = "c"),
                fakeCallTrace(name = "d"),
            ),
            templates = listOf(
                fakeCallTemplate(name = "a"),
                fakeCallTemplate(name = "e"),
                fakeCallTemplate(name = "c")
            )
        )
        val expectedResult = listOf(
            TemplateMatchingResult.Matching(fakeCallTrace(name = "a"), fakeCallTemplate(name = "a")),
            TemplateMatchingResult.NoMatch(fakeCallTemplate(name = "e")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "a")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "b")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "b")),
            TemplateMatchingResult.Matching(fakeCallTrace(name = "c"), fakeCallTemplate(name = "c")),
            TemplateMatchingResult.UnverifiedCall(fakeCallTrace(name = "d")),
        )
        assertEquals(expectedResult, result)
    }
}
