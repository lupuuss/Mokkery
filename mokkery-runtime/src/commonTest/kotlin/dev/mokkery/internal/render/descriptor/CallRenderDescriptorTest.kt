package dev.mokkery.internal.render.descriptor

import dev.mokkery.call
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.createBlockingCallScope
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.fakeFunParam
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CallRenderDescriptorTest {

    @Test
    fun testFunctionCallTemplateToDescriptor() {
        val template = fakeCallTemplate(
            name = "call",
            matchers = arrayOf(
                fakeFunParam<Int>(name = "p1") to ArgMatcher.Any
            )
        )
        val descriptor = template.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("call", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(1, arguments.size)
        val arg0 = assertIs<ArgumentRenderDescriptor.Matcher>(arguments[0])
        assertEquals(template.parameters[0], arg0.parameter)
        assertEquals(ArgMatcher.Any, arg0.matcher)
    }

    @Test
    fun testGetterCallTemplateToDescriptor() {
        val template = fakeCallTemplate(
            name = "<get-foo>",
            matchers = arrayOf(fakeFunParam<Int>(name = "<receiver>") to ArgMatcher.Any)
        )
        val descriptor = template.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("foo", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(1, arguments.size)
        val arg0 = assertIs<ArgumentRenderDescriptor.Matcher>(arguments[0])
        assertEquals(template.parameters[0], arg0.parameter)
        assertEquals(ArgMatcher.Any, arg0.matcher)
    }

    @Test
    fun testSetterCallTemplateToDescriptor() {
        val template = fakeCallTemplate(
            name = "<set-foo>",
            matchers = arrayOf(
                fakeFunParam<Int>(name = "p1") to ArgMatcher.Any
            )
        )
        val descriptor = template.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("foo", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(1, arguments.size)
        val arg0 = assertIs<ArgumentRenderDescriptor.Matcher>(arguments[0])
        assertEquals(template.parameters[0], arg0.parameter)
        assertEquals(ArgMatcher.Any, arg0.matcher)
    }

    @Test
    fun testFunctionCallTraceToDescriptor() {
        val trace = fakeCallTrace(
            name = "call",
            args = listOf(
                fakeCallArg(name = "p1", value = 1),
                fakeCallArg(name = "p2", value = 2),
            )
        )
        val descriptor = trace.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("call", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(2, arguments.size)
        assertEquals(
            expected = trace.args,
            actual = arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testGetterCallTraceToDescriptor() {
        val trace = fakeCallTrace(
            name = "<get-foo>",
            args = listOf(fakeCallArg(name = "p1", value = 1))
        )
        val descriptor = trace.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("foo", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(1, arguments.size)
        assertEquals(
            expected = trace.args,
            actual = arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testSetterCallTraceToDescriptor() {
        val trace = fakeCallTrace(
            name = "<set-foo>",
            args = listOf(fakeCallArg(name = "p1", value = 1))
        )
        val descriptor = trace.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("foo", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(1, arguments.size)
        assertEquals(
            expected = trace.args,
            actual = arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testFunctionCallScopeToDescriptor() {
        val scope = TestMokkeryInstanceScope().createBlockingCallScope(
            name = "call",
            returnType = Int::class,
            args = listOf(
                fakeCallArg(name = "p1", value = 1),
                fakeCallArg(name = "p2", value = 2),
            )
        )
        val descriptor = scope.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("call", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(2, arguments.size)
        assertEquals(
            expected = scope.call.args,
            actual = arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testGetterCallScopeToDescriptor() {
        val scope = TestMokkeryInstanceScope().createBlockingCallScope(
            name = "<get-foo>",
            returnType = Int::class,
            args = listOf(fakeCallArg(name = "p1", value = 1))
        )
        val descriptor = scope.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("foo", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(1, arguments.size)
        assertEquals(
            expected = scope.call.args,
            actual = arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }

    @Test
    fun testSetterCallScopeToDescriptor() {
        val scope = TestMokkeryInstanceScope().createBlockingCallScope(
            name = "<set-foo>",
            returnType = Int::class,
            args = listOf(fakeCallArg(name = "p1", value = 1))
        )
        val descriptor = scope.asCallRenderDescriptor()
        assertEquals(MokkeryInstanceId("mock", 1), descriptor.receiver)
        assertEquals("foo", descriptor.function.name)
        val arguments = descriptor.arguments
        assertEquals(1, arguments.size)
        assertEquals(
            expected = scope.call.args,
            actual = arguments.map { assertIs<ArgumentRenderDescriptor.Value>(it).arg }
        )
    }
}
