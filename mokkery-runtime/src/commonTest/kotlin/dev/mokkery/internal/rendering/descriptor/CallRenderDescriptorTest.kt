package dev.mokkery.internal.rendering.descriptor

import dev.mokkery.call
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.rendering.mokkeryCollection
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.fakeFunParam
import dev.mokkery.test.fakeFunction
import dev.mokkery.test.testBlockingCallScope
import dev.mokkery.test.testRenderingScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CallRenderDescriptorTest {

    private val call = fakeFunction("call", parameters = listOf(fakeFunParam<Int>("p1"), fakeFunParam<Int>("p2")))
    private val callOverload = fakeFunction("call", id = CALL_OVERLOAD_ID, parameters = listOf(fakeFunParam<Int>("p1")))
    private val getter = fakeFunction("<get-foo>", parameters = listOf(fakeFunParam<Int>("<receiver>")))
    private val setter = fakeFunction("<set-foo>", parameters = listOf(fakeFunParam<Int>("p1")))
    private val scope = TestMokkeryInstanceScope(functions = listOf(call, callOverload, getter, setter))

    private fun <R> withRendering(block: MokkeryRenderingScope.() -> R): R = testRenderingScope {
        mokkeryCollection(MokkeryCollection(listOf(scope)))
    }.block()

    @Test
    fun testFunctionCallTemplateToDescriptor() = withRendering {
        val template = fakeCallTemplate(ArgMatcher.Any, functionId = CALL_OVERLOAD_ID)
        val descriptor = template.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("call", descriptor.function.name)
        val arg0 = assertIs<ArgumentRenderDescriptor.Matcher>(descriptor.arguments.single())
        assertEquals(fakeFunParam<Int>("p1"), arg0.parameter)
        assertEquals(ArgMatcher.Any, arg0.matcher)
    }

    @Test
    fun testGetterCallTemplateToDescriptor() = withRendering {
        val template = fakeCallTemplate(ArgMatcher.Any, name = "<get-foo>")
        val descriptor = template.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("foo", descriptor.function.name)
        val arg0 = assertIs<ArgumentRenderDescriptor.Matcher>(descriptor.arguments.single())
        assertEquals(fakeFunParam<Int>("<receiver>"), arg0.parameter)
        assertEquals(ArgMatcher.Any, arg0.matcher)
    }

    @Test
    fun testSetterCallTemplateToDescriptor() = withRendering {
        val template = fakeCallTemplate(ArgMatcher.Any, name = "<set-foo>")
        val descriptor = template.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("foo", descriptor.function.name)
        val arg0 = assertIs<ArgumentRenderDescriptor.Matcher>(descriptor.arguments.single())
        assertEquals(fakeFunParam<Int>("p1"), arg0.parameter)
        assertEquals(ArgMatcher.Any, arg0.matcher)
    }

    @Test
    fun testFunctionCallTraceToDescriptor() = withRendering {
        val trace = fakeCallTrace(name = "call", args = listOf(1, 2))
        val descriptor = trace.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("call", descriptor.function.name)
        assertEquals(
            expected = listOf(fakeCallArg(value = 1, name = "p1"), fakeCallArg(value = 2, name = "p2")),
            actual = descriptor.arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testGetterCallTraceToDescriptor() = withRendering {
        val trace = fakeCallTrace(name = "<get-foo>", args = listOf(1))
        val descriptor = trace.asCallRenderDescriptor()
        assertEquals("foo", descriptor.function.name)
        assertEquals(
            expected = listOf(fakeCallArg(value = 1, name = "<receiver>")),
            actual = descriptor.arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testSetterCallTraceToDescriptor() = withRendering {
        val trace = fakeCallTrace(name = "<set-foo>", args = listOf(1))
        val descriptor = trace.asCallRenderDescriptor()
        assertEquals("foo", descriptor.function.name)
        assertEquals(
            expected = listOf(fakeCallArg(value = 1, name = "p1")),
            actual = descriptor.arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testFunctionCallScopeToDescriptor() {
        val callScope = testBlockingCallScope<Int>(
            name = "call",
            args = listOf(fakeCallArg(value = 1, name = "p1"), fakeCallArg(value = 2, name = "p2")),
        )
        val descriptor = callScope.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("call", descriptor.function.name)
        assertEquals(
            expected = callScope.call.args,
            actual = descriptor.arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testGetterCallScopeToDescriptor() {
        val callScope = testBlockingCallScope<Int>(
            name = "<get-foo>",
            args = listOf(fakeCallArg(value = 1, name = "p1")),
        )
        val descriptor = callScope.asCallRenderDescriptor()
        assertEquals("foo", descriptor.function.name)
        assertEquals(
            expected = callScope.call.args,
            actual = descriptor.arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testSetterCallScopeToDescriptor() {
        val callScope = testBlockingCallScope<Int>(
            name = "<set-foo>",
            args = listOf(fakeCallArg(value = 1, name = "p1")),
        )
        val descriptor = callScope.asCallRenderDescriptor()
        assertEquals("foo", descriptor.function.name)
        assertEquals(
            expected = callScope.call.args,
            actual = descriptor.arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }
}

private const val CALL_OVERLOAD_ID = 3L
