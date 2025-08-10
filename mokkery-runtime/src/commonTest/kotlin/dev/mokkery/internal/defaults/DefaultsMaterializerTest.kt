package dev.mokkery.internal.defaults

import dev.mokkery.context.CallArgument
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.matcher.DefaultValueMatcher
import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.internal.mockId
import dev.mokkery.internal.MocksCollection
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestMokkeryInstanceScope
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultsMaterializerTest {

    private object FakeExtractor

    private val defaultsExtractorFactory = object : DefaultsExtractorFactory {
        override fun createDefaultsExtractor(): Any = FakeExtractor
    }
    private val scope = TestMokkeryInstanceScope(context = defaultsExtractorFactory)
    private val mocks = MocksCollection(listOf(scope))
    private val materializer = DefaultsMaterializer(mocks)
    private val trace = CallTrace(
        mockId = scope.mockId,
        name = "call",
        args = listOf(
            CallArgument(1, "i", Int::class, false),
            CallArgument("Hello!", "j", String::class, false),
        ),
        orderStamp = 0
    )

    @Test
    fun testReturnsIdentityWhenNoDefaultMatchers() {
        val template = CallTemplate(
            mockId = scope.mockId,
            name = "call",
            signature = "call(i: Int, j: String)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1),
                "j" to ArgMatcher.Any
            )
        )
        assertEquals(template, materializer.materialize(trace, template))
    }

    @Test
    fun testReturnsMaterializedMatchersForBlockingCallWhenDefaultMatcherPresent() {
        var objectPassed: Any? = null
        var argumentsPassed: List<Any?>? = null
        val caller: (Any, List<Any?>) -> Nothing = { obj, args ->
            objectPassed = obj
            argumentsPassed = args
            throwArguments(1, "Materialized!")
        }
        val template = CallTemplate(
            mockId = scope.mockId,
            name = "call",
            signature = "call(i: Int, j: String)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1),
                "j" to DefaultValueMatcher(
                    mask = 0b10L,
                    caller = caller,
                    isSuspend = false
                )
            )
        )
        val resultTemplate = materializer.materialize(trace, template)
        assertEquals(template.mockId, resultTemplate.mockId)
        assertEquals(template.name, resultTemplate.name)
        assertEquals(template.signature, resultTemplate.signature)
        assertEquals(FakeExtractor, objectPassed)
        assertEquals(trace.args.map { it.value }, argumentsPassed)
        val expectedMatchers = mapOf(
            "i" to ArgMatcher.Equals(1),
            "j" to MaterializedDefaultValueMatcher("Materialized!")
        )
        assertEquals(expectedMatchers, resultTemplate.matchers)
    }

    @Test
    fun testReturnsMaterializedMatchersForSuspendCallWhenDefaultMatcherPresent() {
        var objectPassed: Any? = null
        var argumentsPassed: List<Any?>? = null
        val caller: suspend (Any, List<Any?>) -> Nothing = { obj, args ->
            objectPassed = obj
            argumentsPassed = args
            throwArguments(3, "Hello!")
        }
        val template = CallTemplate(
            mockId = scope.mockId,
            name = "call",
            signature = "call(i: Int, j: String)",
            matchers = mapOf(
                "i" to DefaultValueMatcher(
                    mask = 0b01L,
                    caller = caller,
                    isSuspend = true
                ),
                "j" to ArgMatcher.Equals("Hello!")
            )
        )
        val resultTemplate = materializer.materialize(trace, template)
        assertEquals(template.mockId, resultTemplate.mockId)
        assertEquals(template.name, resultTemplate.name)
        assertEquals(template.signature, resultTemplate.signature)
        assertEquals(FakeExtractor, objectPassed)
        assertEquals(trace.args.map { it.value }, argumentsPassed)
        val expectedMatchers = mapOf(
            "i" to MaterializedDefaultValueMatcher(3),
            "j" to ArgMatcher.Equals("Hello!")
        )
        assertEquals(expectedMatchers, resultTemplate.matchers)
    }
}
