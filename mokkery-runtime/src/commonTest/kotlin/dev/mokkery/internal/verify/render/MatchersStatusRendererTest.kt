package dev.mokkery.internal.verify.render

import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.fakeDefaultValueMatcher
import dev.mokkery.test.fakeFunParam
import dev.mokkery.test.testRendering
import kotlin.test.Test

class MatchersStatusRendererTest {

    private val argMatcherRenderer = TestRenderer<ArgMatcher<*>>(MokkeryRendering.argMatcherKey) { "RENDERER_MATCHER" }
    private val descriptionRenderer = TestRenderer<Any?>(MokkeryRendering.descriptionKey) { "RENDERER_VALUE" }
    private val context = argMatcherRenderer + descriptionRenderer
    private val renderer = MatchersStatusRenderer

    private val trace = fakeCallTrace(
        args = listOf(
            fakeCallArg(name = "a", value = "string"),
            fakeCallArg(name = "b", value = 1),
            fakeCallArg(name = "c", value = listOf("a", "b")),
        )
    )

    @Test
    fun testRendersMatchersWithCallArgsProperly() {
        val template = fakeCallTemplate(
            fakeFunParam<String>("a") to ArgMatcher.Equals("string"),
            fakeFunParam<String>("b") to ArgMatcher.Any,
            fakeFunParam<String>("c") to ArgMatcher.Equals(listOf("a", "b", "c")),
        )
        testRendering(context) {
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
            fakeFunParam<String>("a") to ArgMatcher.Equals("string"),
            fakeFunParam<String>("b") to ArgMatcher.Any,
            fakeFunParam<String>("c") to MaterializedDefaultValueMatcher(listOf("a", "b", "c")),
        )
        testRendering(context) {
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
            fakeFunParam<String>("a") to ArgMatcher.Equals("str"),
            fakeFunParam<String>("b") to ArgMatcher.Any,
            fakeFunParam<String>("c") to fakeDefaultValueMatcher(),
        )
        testRendering(context) {
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
