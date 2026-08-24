package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.fakeDefaultValueMatcher
import dev.mokkery.test.fakeFunParam
import dev.mokkery.test.fakeFunction
import dev.mokkery.test.testRendering
import kotlin.test.Test

class MatchersStatusRendererTest {

    private val argMatcherRenderer = TestRenderer<ArgMatcher<*>>(MokkeryRendering.argMatcherKey) { "RENDERER_MATCHER" }
    private val descriptionRenderer = TestRenderer<Any?>(MokkeryRendering.descriptionKey) { "RENDERER_VALUE" }
    private val context = argMatcherRenderer + descriptionRenderer
    private val renderer = MatchersStatusRenderer

    private val function = fakeFunction(
        name = "call",
        parameters = listOf(
            fakeFunParam<String>("a"),
            fakeFunParam<Int>("b"),
            fakeFunParam<List<String>>("c"),
        ),
    )
    private val scope = TestMokkeryInstanceScope(functions = listOf(function))
    private val instances = MokkeryCollection(listOf(scope))

    private val trace = fakeCallTrace(
        name = "call",
        args = listOf("string", 1, listOf("a", "b")),
    )

    @Test
    fun testRendersMatchersWithCallArgsProperly() {
        val template = fakeCallTemplate(
            ArgMatcher.Equals("string"),
            ArgMatcher.Any,
            ArgMatcher.Equals(listOf("a", "b", "c")),
            name = "call",
        )
        testRendering(context, instances) {
            renderer.assert(template to trace) {
                """
                    [+] a: RENDERER_MATCHER ~ RENDERER_VALUE
                    [+] b: RENDERER_MATCHER ~ RENDERER_VALUE
                    [-] c:
                       expect: RENDERER_MATCHER
                       actual: RENDERER_VALUE

                """.trimIndent()
            }
        }
    }

    @Test
    fun testRendersDefaultsWithCallArgsWhenOtherMatchersMatching() {
        val template = fakeCallTemplate(
            ArgMatcher.Equals("string"),
            ArgMatcher.Any,
            MaterializedDefaultValueMatcher(listOf("a", "b", "c")),
            name = "call",
        )
        testRendering(context, instances) {
            renderer.assert(template to trace) {
                """
                    [+] a: RENDERER_MATCHER ~ RENDERER_VALUE
                    [+] b: RENDERER_MATCHER ~ RENDERER_VALUE
                    [-] c:
                       expect: RENDERER_MATCHER
                       actual: RENDERER_VALUE

                """.trimIndent()
            }
        }
    }

    @Test
    fun testRendersSkipsRenderingDefaultsWhenOtherMatchersDoesNotMatch() {
        val template = fakeCallTemplate(
            ArgMatcher.Equals("str"),
            ArgMatcher.Any,
            fakeDefaultValueMatcher(),
            name = "call",
        )
        testRendering(context, instances) {
            renderer.assert(template to trace) {
                """
                    [-] a:
                       expect: RENDERER_MATCHER
                       actual: RENDERER_VALUE
                    [+] b: RENDERER_MATCHER ~ RENDERER_VALUE
                    [?] c:
                       expect: default() => Cannot be determined, because other matchers don't match!
                       actual: RENDERER_VALUE

                """.trimIndent()
            }
        }
    }
}
