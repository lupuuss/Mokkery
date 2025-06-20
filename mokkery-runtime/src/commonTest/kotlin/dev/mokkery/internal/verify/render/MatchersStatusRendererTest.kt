package dev.mokkery.internal.verify.render

import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.StubRenderer
import dev.mokkery.test.TestDefaultsMaterializer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.fakeDefaultValueMatcher
import kotlin.test.Test

class MatchersStatusRendererTest {

    private val defaultsMaterializer = TestDefaultsMaterializer()
    private val renderer = MatchersStatusRenderer(
        materializer = defaultsMaterializer,
        valueRenderer = StubRenderer("VALUE"),
        matcherRenderer = StubRenderer("MATCHER"),
    )
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
            matchers = mapOf(
                "a" to ArgMatcher.Equals("string"),
                "b" to ArgMatcher.Any,
                "c" to ArgMatcher.Equals(listOf("a", "b", "c")),
            )
        )
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

    @Test
    fun testRendersDefaultsWithCallArgsWhenOtherMatchersMatching() {
        val template = fakeCallTemplate(
            matchers = mapOf(
                "a" to ArgMatcher.Equals("string"),
                "b" to ArgMatcher.Any,
                "c" to fakeDefaultValueMatcher(),
            )
        )
        defaultsMaterializer.calls = { _, it ->
            it.copy(matchers = it.matchers.plus("c" to MaterializedDefaultValueMatcher(listOf("a", "b", "c"))))
        }
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

    @Test
    fun testRendersSkipsRenderingDefaultsWhenOtherMatchersDoesNotMatch() {
        val template = fakeCallTemplate(
            matchers = mapOf(
                "a" to ArgMatcher.Equals("str"),
                "b" to ArgMatcher.Any,
                "c" to fakeDefaultValueMatcher(),
            )
        )
        defaultsMaterializer.calls = { _, it ->
            it.copy(matchers = it.matchers.plus("c" to MaterializedDefaultValueMatcher(listOf("a", "b", "c"))))
        }
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
