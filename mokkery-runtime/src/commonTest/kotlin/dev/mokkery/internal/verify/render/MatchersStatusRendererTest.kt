package dev.mokkery.internal.verify.render

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.StubRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test

class MatchersStatusRendererTest {

    private val renderer = MatchersStatusRenderer(
        valueRenderer = StubRenderer("VALUE"),
        matcherRenderer = StubRenderer("MATCHER")
    )
    private val trace = fakeCallTrace(
        args = listOf(
            fakeCallArg(name = "a", value = "string"),
            fakeCallArg(name = "b", value = 1),
            fakeCallArg(name = "c", value = listOf("a", "b")),
        )
    )
    private val template = fakeCallTemplate(
        matchers = mapOf(
            "a" to ArgMatcher.Equals("string"),
            "b" to ArgMatcher.Any,
            "c" to ArgMatcher.Equals(listOf("a", "b", "c")),
        )
    )

    @Test
    fun testRendersMatchersWithCallArgsProperly() {
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
