package dev.mokkery.internal.defaults

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MutableMokkeryInstanceScope
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.matcher.MaterializedDefaultValueMatcher
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeFunParam
import dev.mokkery.test.fakeFunction
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.test.TestInstanceContracts
import dev.mokkery.test.fakeCallEntry
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultsMaterializerTest {

    private object FakeExtractor : MutableMokkeryInstanceScope {
        override var mokkeryContext: MokkeryContext = MokkeryContext.Empty
    }

    private val scope = TestMokkeryInstanceScope(
        functions = listOf(fakeFunction("call", parameters = listOf(fakeFunParam<Int>("i"), fakeFunParam<String>("j")))),
        context = TestInstanceContracts(defaultsExtractor = FakeExtractor),
    )
    private val instances = MokkeryCollection(listOf(scope))
    private val materializer = DefaultsMaterializer(instances)
    private val entry = fakeCallEntry(name = "call", args = listOf(1, "Hello!"))

    private fun template(vararg matchers: ArgMatcher<Any?>) = fakeCallTemplate(*matchers, name = "call")

    @Test
    fun testReturnsIdentityWhenNoDefaultMatchers() {
        val template = template(ArgMatcher.Equals(1), ArgMatcher.Any)
        assertEquals(template, materializer.materialize(template, entry))
    }

    @Test
    fun testReturnsMaterializedMatchersForBlockingCallWhenDefaultMatcherPresent() {
        var objectPassed: Any? = null
        var argumentsPassed: List<Any?>? = null
        val caller: (Any, List<Any?>) -> Nothing = { obj, args ->
            objectPassed = obj
            argumentsPassed = args
            throw ArgumentsExtractedException(listOf(1, "Materialized!"))
        }
        val template = template(
            ArgMatcher.Equals(1),
            DefaultValuesMatcher(
                mask = 0b10L,
                extractingFunction = caller,
                isExtractingFunctionSuspend = false
            )
        )
        val resultTemplate = materializer.materialize(template, entry)
        assertEquals(template.instanceId, resultTemplate.instanceId)
        assertEquals(template.functionId, resultTemplate.functionId)
        assertEquals(FakeExtractor, objectPassed)
        assertEquals(entry.args, argumentsPassed)
        assertEquals<List<ArgMatcher<Any?>>>(
            listOf(ArgMatcher.Equals(1), MaterializedDefaultValueMatcher("Materialized!")),
            resultTemplate.matchers
        )
    }

    @Test
    fun testFailsWithMokkeryErrorWhenMaskYieldsFewerDefaultsThanMatchers() {
        val template = template(
            ArgMatcher.Equals(1),
            DefaultValuesMatcher(
                // no bit set, so nothing is extracted despite the matcher expecting a default
                mask = 0L,
                extractingFunction = { _: Any, _: List<Any?> ->
                    throw ArgumentsExtractedException(listOf(1, "Materialized!"))
                },
                isExtractingFunctionSuspend = false
            )
        )
        val exception = assertFailsWith<MokkeryRuntimeException> { materializer.materialize(template, entry) }
        assertContains(exception.message.orEmpty(), "Failed to materialize the default value of `j` in `call`!")
    }

    @Test
    fun testReturnsMaterializedMatchersForSuspendCallWhenDefaultMatcherPresent() {
        var objectPassed: Any? = null
        var argumentsPassed: List<Any?>? = null
        val caller: suspend (Any, List<Any?>) -> Nothing = { obj, args ->
            objectPassed = obj
            argumentsPassed = args
            throw ArgumentsExtractedException(listOf(3, "Hello!"))
        }
        val template = template(
            DefaultValuesMatcher(
                mask = 0b01L,
                extractingFunction = caller,
                isExtractingFunctionSuspend = true
            ),
            ArgMatcher.Equals("Hello!")
        )
        val resultTemplate = materializer.materialize(template, entry)
        assertEquals(template.instanceId, resultTemplate.instanceId)
        assertEquals(template.functionId, resultTemplate.functionId)
        assertEquals(FakeExtractor, objectPassed)
        assertEquals(entry.args, argumentsPassed)
        assertEquals<List<ArgMatcher<Any?>>>(
            listOf(MaterializedDefaultValueMatcher(3), ArgMatcher.Equals("Hello!")),
            resultTemplate.matchers
        )
    }
}
