package dev.mokkery.internal.defaults

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.MokkeryTools
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
        context = TestInstanceContracts(defaultsExtractor = FakeExtractor) + MokkeryTools.default(),
    )
    private val instances = MokkeryCollection(listOf(scope))
    private val materializer = DefaultsMaterializer(instances)
    private val entry = fakeCallEntry(name = "call", args = listOf(1, "Hello!"))

    private fun template(vararg matchers: ArgMatcher<Any?>) = fakeCallTemplate(*matchers, name = "call")

    private class Identity {
        override fun toString(): String = "Identity"
    }

    private fun defaultsMatcher(mask: Long, values: () -> List<Any?>) = DefaultValuesMatcher(
        mask = mask,
        extractingFunction = { _: Any, _: List<Any?> -> throw ArgumentsExtractedException(values()) },
        isExtractingFunctionSuspend = false
    )

    private fun expectedNonDeterministicMessage(
        parameter: String,
        first: Any?,
        second: Any?,
        call: String = "mock(1).call(i = 1, j = default())",
    ) = "Call template `$call` relies on the default value of" +
                " `$parameter` in `call`," +
                " but Mokkery cannot match on it -" +
                " evaluating that default twice produced values that are not equal ($first and $second)." +
                " Either the default is not deterministic (random values, current time, counters, etc.)," +
                " or its value does not implement structural equality." +
                " Pass that argument explicitly in the `every`/`verify` block that registered this template."

    @Test
    fun testCheckNonDeterministicDefaultsPassesWhenNoDefaultMatchers() {
        val template = template(ArgMatcher.Equals(1), ArgMatcher.Any)
        materializer.checkNonDeterministicDefaults(template, entry, template)
    }

    @Test
    fun testCheckNonDeterministicDefaultsPassesWhenDefaultsAreStable() {
        val template = template(ArgMatcher.Equals(1), defaultsMatcher(0b10L) { listOf(1, "Materialized!") })
        val materialized = materializer.materialize(template, entry)
        materializer.checkNonDeterministicDefaults(template, entry, materialized)
    }

    @Test
    fun testCheckNonDeterministicDefaultsFailsWhenDefaultChangesBetweenEvaluations() {
        var counter = 0
        val template = template(ArgMatcher.Equals(1), defaultsMatcher(0b10L) { listOf(1, counter++) })
        val materialized = materializer.materialize(template, entry)
        val exception = assertFailsWith<MokkeryRuntimeException> {
            materializer.checkNonDeterministicDefaults(template, entry, materialized)
        }
        assertEquals(expectedNonDeterministicMessage("j", 0, 1), exception.message)
    }

    @Test
    fun testCheckNonDeterministicDefaultsFailsWhenDefaultValueHasNoStructuralEquality() {
        val template = template(ArgMatcher.Equals(1), defaultsMatcher(0b10L) { listOf(1, Identity()) })
        val materialized = materializer.materialize(template, entry)
        val exception = assertFailsWith<MokkeryRuntimeException> {
            materializer.checkNonDeterministicDefaults(template, entry, materialized)
        }
        assertEquals(expectedNonDeterministicMessage("j", "Identity", "Identity"), exception.message)
    }

    @Test
    fun testCheckNonDeterministicDefaultsReportsUnstableDefaultWhenAnotherDefaultIsStable() {
        var counter = 0
        val template = template(
            defaultsMatcher(0b11L) { listOf(1, counter++) },
            defaultsMatcher(0b11L) { error("Only the first default values matcher is used!") },
        )
        val materialized = materializer.materialize(template, entry)
        val exception = assertFailsWith<MokkeryRuntimeException> {
            materializer.checkNonDeterministicDefaults(template, entry, materialized)
        }
        assertEquals(
            expectedNonDeterministicMessage("j", 0, 1, call = "mock(1).call(i = default(), j = default())"),
            exception.message
        )
    }

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
