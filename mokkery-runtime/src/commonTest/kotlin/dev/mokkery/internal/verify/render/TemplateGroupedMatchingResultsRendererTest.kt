package dev.mokkery.internal.verify.render

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.test.StubRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test

class TemplateGroupedMatchingResultsRendererTest {

    private val renderer = TemplateGroupedMatchingResultsRenderer(
        indentation = 3,
        matchersFailuresRenderer = StubRenderer("MATCHERS", StubRenderer.Mode.RepeatWithBreak(2)),
        traceRenderer = StubRenderer("TRACE")
    )

    private val template = fakeCallTemplate()
    private val traces = listOf(fakeCallTrace(), fakeCallTrace())
    private val singleTrace = listOf(fakeCallTrace())

    @Test
    fun testRendersProperlyMatchingGroup() {
        renderer.assert(fakeMatchingResults(template, CallMatchResult.Matching to traces)) {
            """
            Results for mock@1:
            # Matching calls:
               RENDERER_TRACE
               RENDERER_TRACE
            
            """.trimIndent()
        }
    }

    @Test
    fun testRendersProperlyFailingMatchersGroup() {
        renderer.assert(fakeMatchingResults(template, CallMatchResult.SameReceiverMethodSignature to traces)) {
            """
            Results for mock@1:
            # Calls to the same method with failing matchers:
               RENDERER_TRACE
                  RENDERER_MATCHERS
                  RENDERER_MATCHERS
               RENDERER_TRACE
                  RENDERER_MATCHERS
                  RENDERER_MATCHERS
            
               """.trimIndent()
        }
    }

    @Test
    fun testRendersProperlyOverloadsGroup() {
        renderer.assert(fakeMatchingResults(template, CallMatchResult.SameReceiverMethodOverload to traces)) {
            """
            Results for mock@1:
            # Calls to the same overload:
               RENDERER_TRACE
               RENDERER_TRACE
            
            """.trimIndent()
        }
    }

    @Test
    fun testRendersProperlyOtherCallsGroup() {
        renderer.assert(fakeMatchingResults(template, CallMatchResult.SameReceiver to traces)) {
            """
            Results for mock@1:
            # Other calls to this mock:
               RENDERER_TRACE
               RENDERER_TRACE
            
            """.trimIndent()
        }
    }

    @Test
    fun testRendersAllGroupsInCorrectOrder() {
        renderer.assert(
            fakeMatchingResults(
                template,
                CallMatchResult.Matching to singleTrace,
                CallMatchResult.SameReceiverMethodSignature to singleTrace,
                CallMatchResult.SameReceiverMethodOverload to singleTrace,
                CallMatchResult.SameReceiver to singleTrace,
            )
        ) {
            """
            Results for mock@1:
            # Matching calls:
               RENDERER_TRACE
            # Calls to the same method with failing matchers:
               RENDERER_TRACE
                  RENDERER_MATCHERS
                  RENDERER_MATCHERS
            # Calls to the same overload:
               RENDERER_TRACE
            # Other calls to this mock:
               RENDERER_TRACE
            
            """.trimIndent()
        }
    }

    @Test
    fun testSkipsEmptyGroups() {
        renderer.assert(
            fakeMatchingResults(
                template,
                CallMatchResult.Matching to singleTrace,
                CallMatchResult.SameReceiverMethodSignature to emptyList(),
                CallMatchResult.SameReceiverMethodOverload to singleTrace,
                CallMatchResult.SameReceiver to emptyList(),
            )
        ) {
            """
            Results for mock@1:
            # Matching calls:
               RENDERER_TRACE
            # Calls to the same overload:
               RENDERER_TRACE
            
            """.trimIndent()
        }
    }

    @Test
    fun testRendersEmptyState() {
        renderer.assert(
            fakeMatchingResults(
                template,
                CallMatchResult.Matching to emptyList(),
                CallMatchResult.SameReceiverMethodSignature to emptyList(),
                CallMatchResult.SameReceiverMethodOverload to emptyList(),
                CallMatchResult.SameReceiver to emptyList(),
            )
        ) {
            """
            Results for mock@1:
            # No calls to this mock!
            
            """.trimIndent()
        }
    }

    private fun fakeMatchingResults(
        template: CallTemplate,
        vararg groups: Pair<CallMatchResult, List<CallTrace>>,
    ) = TemplateGroupedMatchingResults(template, groups.toMap())
}
