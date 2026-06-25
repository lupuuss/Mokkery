package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test

class TemplateGroupedMatchingResultsRendererTest {

    private val instanceIdRenderer = TestRenderer<MokkeryInstanceId>(MokkeryRendering.instanceIdKey) { "RENDERER_MOCK_ID" }
    private val traceRenderer = TestRenderer<CallTrace>(MokkeryRendering.callTraceKey) { "RENDERER_TRACE" }
    private val matcherStatusRenderer = TestRenderer<Pair<CallTemplate, CallTrace>>(VerifyRendering.matcherStatus) {
        "RENDERER_MATCHERS\n"
    }

    private val context = instanceIdRenderer + traceRenderer + matcherStatusRenderer
    private val renderer = TemplateGroupedMatchingResultsRenderer

    private val template = fakeCallTemplate()
    private val traces = listOf(fakeCallTrace(), fakeCallTrace())
    private val singleTrace = listOf(fakeCallTrace())

    @Test
    fun testRendersProperlyMatchingGroup() {
        testRendering(context) {
            renderer.assert(fakeMatchingResults(template, CallMatchResult.Matching to traces)) {
                """
                Results for RENDERER_MOCK_ID:
                # Matching calls:
                  RENDERER_TRACE
                  RENDERER_TRACE
                
                """.trimIndent()
            }
        }
    }

    @Test
    fun testRendersProperlyFailingMatchersGroup() {
        testRendering(context) {
            renderer.assert(fakeMatchingResults(template, CallMatchResult.SameReceiverMethodSignature to traces)) {
                """
                Results for RENDERER_MOCK_ID:
                # Calls to the same method with failing matchers:
                  RENDERER_TRACE
                    RENDERER_MATCHERS
                  RENDERER_TRACE
                    RENDERER_MATCHERS
                
                """.trimIndent()
            }
        }
    }

    @Test
    fun testRendersProperlyOverloadsGroup() {
        testRendering(context) {
            renderer.assert(fakeMatchingResults(template, CallMatchResult.SameReceiverMethodOverload to traces)) {
                """
                Results for RENDERER_MOCK_ID:
                # Calls to the same overload:
                  RENDERER_TRACE
                  RENDERER_TRACE
                
                """.trimIndent()
            }
        }
    }

    @Test
    fun testRendersProperlyOtherCallsGroup() {
        testRendering(context) {
            renderer.assert(fakeMatchingResults(template, CallMatchResult.SameReceiver to traces)) {
                """
                Results for RENDERER_MOCK_ID:
                # Other calls to this mock:
                  RENDERER_TRACE
                  RENDERER_TRACE
                
                """.trimIndent()
            }
        }
    }

    @Test
    fun testRendersAllGroupsInCorrectOrder() {
        testRendering(context) {
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
                Results for RENDERER_MOCK_ID:
                # Matching calls:
                  RENDERER_TRACE
                # Calls to the same method with failing matchers:
                  RENDERER_TRACE
                    RENDERER_MATCHERS
                # Calls to the same overload:
                  RENDERER_TRACE
                # Other calls to this mock:
                  RENDERER_TRACE
                
                """.trimIndent()
            }
        }
    }

    @Test
    fun testSkipsEmptyGroups() {
        testRendering(context) {
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
                Results for RENDERER_MOCK_ID:
                # Matching calls:
                  RENDERER_TRACE
                # Calls to the same overload:
                  RENDERER_TRACE
                
                """.trimIndent()
            }
        }
    }

    @Test
    fun testRendersEmptyState() {
        testRendering(context) {
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
                Results for RENDERER_MOCK_ID:
                # No calls to this mock!
                
                """.trimIndent()
            }
        }
    }

    private fun fakeMatchingResults(
        template: CallTemplate,
        vararg groups: Pair<CallMatchResult, List<CallTrace>>,
    ) = TemplateGroupedMatchingResults(template, groups.toMap())
}
