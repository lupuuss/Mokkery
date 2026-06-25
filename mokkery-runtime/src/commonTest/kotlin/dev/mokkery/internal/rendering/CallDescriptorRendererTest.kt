package dev.mokkery.internal.rendering

import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.rendering.descriptor.ArgumentRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.CallRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.FunctionRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.GetterRenderDescriptor
import dev.mokkery.internal.rendering.descriptor.SetterRenderDescriptor
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeFunParam
import dev.mokkery.test.testRenderingScope
import kotlin.test.Test
import kotlin.test.assertEquals

class CallDescriptorRendererTest {

    private val valueRenderer = TestRenderer<Any?>(MokkeryRendering.descriptionKey) { it.toString() }
    private val matcherRenderer = TestRenderer<ArgMatcher<*>>(MokkeryRendering.argMatcherKey) { "M($it)" }
    private val instanceIdRenderer = TestRenderer<MokkeryInstanceId>(MokkeryRendering.instanceIdKey) { it.toString() }

    @Test
    fun testRenderGetterWithReceiverOffForCallArguments() {
        assertRendered(
            renderReceiver = false,
            expected = "(<receiver_1> = 1, <receiver_2> = 2).foo",
            descriptor = testDescriptor(
                getterDescriptor("foo"),
                argDescriptor(fakeCallArg(value = 1, name = "<receiver_1>")),
                argDescriptor(fakeCallArg(value = 2, name = "<receiver_2>")),
            ),
        )
    }

    @Test
    fun testRenderGetterWithReceiverOnForCallArguments() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).(<receiver_1> = 1, <receiver_2> = 2).foo",
            descriptor = testDescriptor(
                getterDescriptor("foo"),
                argDescriptor(fakeCallArg(value = 1, name = "<receiver_1>")),
                argDescriptor(fakeCallArg(value = 2, name = "<receiver_2>")),
            ),
        )
    }

    @Test
    fun testRenderGetterWithReceiverOffForNoArguments() {
        assertRendered(
            renderReceiver = false,
            expected = "foo",
            descriptor = testDescriptor(getterDescriptor("foo")),
        )
    }

    @Test
    fun testRenderGetterWithReceiverOnForNoArguments() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).foo",
            descriptor = testDescriptor(getterDescriptor("foo")),
        )
    }

    @Test
    fun testRenderGetterWithReceiverOffForMatchers() {
        assertRendered(
            renderReceiver = false,
            expected = "(<receiver_1> = M(Any), <receiver_2> = M(Any)).foo",
            descriptor = testDescriptor(
                getterDescriptor("foo"),
                argDescriptor(fakeFunParam<Int>(name = "<receiver_1>"), ArgMatcher.Any),
                argDescriptor(fakeFunParam<Int>(name = "<receiver_2>"), ArgMatcher.Any),
            ),
        )
    }

    @Test
    fun testRenderGetterWithReceiverOnForMatchers() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).(<receiver_1> = M(Any), <receiver_2> = M(Any)).foo",
            descriptor = testDescriptor(
                getterDescriptor("foo"),
                argDescriptor(fakeFunParam<Int>(name = "<receiver_1>"), ArgMatcher.Any),
                argDescriptor(fakeFunParam<Int>(name = "<receiver_2>"), ArgMatcher.Any),
            ),
        )
    }

    @Test
    fun testRenderSetterWithReceiverOffForCallArguments() {
        assertRendered(
            renderReceiver = false,
            expected = "(<receiver_1> = 1, <receiver_2> = 2).foo = 5",
            descriptor = testDescriptor(
                setterDescriptor("foo"),
                argDescriptor(fakeCallArg(value = 1, name = "<receiver_1>")),
                argDescriptor(fakeCallArg(value = 2, name = "<receiver_2>")),
                argDescriptor(fakeCallArg(value = 5, name = "value"))
            ),
        )
    }

    @Test
    fun testRenderSetterWithReceiverOnForCallArguments() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).(<receiver_1> = 1, <receiver_2> = 2).foo = 5",
            descriptor = testDescriptor(
                setterDescriptor("foo"),
                argDescriptor(fakeCallArg(value = 1, name = "<receiver_1>")),
                argDescriptor(fakeCallArg(value = 2, name = "<receiver_2>")),
                argDescriptor(fakeCallArg(value = 5, name = "value"))
            ),
        )
    }

    @Test
    fun testRenderSetterWithReceiverOffForSingleArgument() {
        assertRendered(
            renderReceiver = false,
            expected = "foo = 5",
            descriptor = testDescriptor(
                setterDescriptor("foo"),
                argDescriptor(fakeCallArg(value = 5, name = "value"))
            ),
        )
    }

    @Test
    fun testRenderSetterWithReceiverOnForForSingleArgument() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).foo = 5",
            descriptor = testDescriptor(
                setterDescriptor("foo"),
                argDescriptor(fakeCallArg(value = 5, name = "value"))
            ),
        )
    }

    @Test
    fun testRenderSetterWithReceiverOffForSingleMatcher() {
        assertRendered(
            renderReceiver = false,
            expected = "foo = M(Any)",
            descriptor = testDescriptor(
                setterDescriptor("foo"),
                argDescriptor(fakeFunParam<Int>(name = "value"), ArgMatcher.Any),
            ),
        )
    }

    @Test
    fun testRenderSetterWithReceiverOnForForSingleMatcher() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).foo = M(Any)",
            descriptor = testDescriptor(
                setterDescriptor("foo"),
                argDescriptor(fakeFunParam<Int>(name = "value"), ArgMatcher.Any),
            ),
        )
    }

    @Test
    fun testRenderSetterWithReceiverOffForMatchers() {
        assertRendered(
            renderReceiver = false,
            expected = "(<receiver_1> = M(Any), <receiver_2> = M(Any)).foo = M(Any)",
            descriptor = testDescriptor(
                setterDescriptor("foo"),
                argDescriptor(fakeFunParam<Int>(name = "<receiver_1>"), ArgMatcher.Any),
                argDescriptor(fakeFunParam<Int>(name = "<receiver_2>"), ArgMatcher.Any),
                argDescriptor(fakeFunParam<Int>(name = "value"), ArgMatcher.Any),
            ),
        )
    }

    @Test
    fun testRenderSetterWithReceiverOnForMatchers() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).(<receiver_1> = M(Any), <receiver_2> = M(Any)).foo = M(Any)",
            descriptor = testDescriptor(
                setterDescriptor("foo"),
                argDescriptor(fakeFunParam<Int>(name = "<receiver_1>"), ArgMatcher.Any),
                argDescriptor(fakeFunParam<Int>(name = "<receiver_2>"), ArgMatcher.Any),
                argDescriptor(fakeFunParam<Int>(name = "value"), ArgMatcher.Any),
            ),
        )
    }

    @Test
    fun testRenderFunctionWithReceiverOffForCallArguments() {
        assertRendered(
            renderReceiver = false,
            expected = "foo(p1 = 1, p2 = 5)",
            descriptor = testDescriptor(
                funcDescriptor("foo"),
                argDescriptor(fakeCallArg(value = 1, name = "p1")),
                argDescriptor(fakeCallArg(value = 5, name = "p2"))
            ),
        )
    }

    @Test
    fun testRenderFunctionWithReceiverOnForCallArguments() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).foo(p1 = 1, p2 = 5)",
            descriptor = testDescriptor(
                funcDescriptor("foo"),
                argDescriptor(fakeCallArg(value = 1, name = "p1")),
                argDescriptor(fakeCallArg(value = 5, name = "p2"))
            ),
        )
    }

    @Test
    fun testRenderFunctionWithReceiverOffForNoArguments() {
        assertRendered(
            renderReceiver = false,
            expected = "foo()",
            descriptor = testDescriptor(funcDescriptor("foo")),
        )
    }

    @Test
    fun testRenderFunctionWithReceiverOnForNoArguments() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).foo()",
            descriptor = testDescriptor(funcDescriptor("foo")),
        )
    }

    @Test
    fun testRenderFunctionWithReceiverOffForMatchers() {
        assertRendered(
            renderReceiver = false,
            expected = "foo(p1 = M(Any), p2 = M(Any))",
            descriptor = testDescriptor(
                funcDescriptor("foo"),
                argDescriptor(fakeFunParam<Int>(name = "p1"), ArgMatcher.Any),
                argDescriptor(fakeFunParam<Int>(name = "p2"), ArgMatcher.Any),
            ),
        )
    }

    @Test
    fun testRenderFunctionWithReceiverOnForMatchers() {
        assertRendered(
            renderReceiver = true,
            expected = "Mock(1).foo(p1 = M(Any), p2 = M(Any))",
            descriptor = testDescriptor(
                funcDescriptor("foo"),
                argDescriptor(fakeFunParam<Int>(name = "p1"), ArgMatcher.Any),
                argDescriptor(fakeFunParam<Int>(name = "p2"), ArgMatcher.Any),
            ),
        )
    }

    private fun assertRendered(
        renderReceiver: Boolean = true,
        expected: String,
        descriptor: CallRenderDescriptor
    ) {
        val renderer = MokkeryRendering.callDescriptorImpl
        val scope = testRenderingScope(valueRenderer + matcherRenderer + instanceIdRenderer)
            .configured { receiverRendering(renderReceiver) }
        context(scope) {
            assertEquals(expected, renderer.render(descriptor))
        }
    }

    private fun testDescriptor(
        function: FunctionRenderDescriptor,
        vararg arguments: ArgumentRenderDescriptor
    ) = object : CallRenderDescriptor {
        override val receiver = MokkeryInstanceId("Mock", 1)
        override val function = function
        override val arguments = arguments.asList()
    }

    private fun funcDescriptor(name: String) = object : FunctionRenderDescriptor {
        override val name = name
    }

    private fun getterDescriptor(name: String) = object : GetterRenderDescriptor {
        override val name = name
    }

    private fun setterDescriptor(name: String) = object : SetterRenderDescriptor {
        override val name = name
    }

    private fun argDescriptor(arg: CallArgument) = ArgumentRenderDescriptor.Value(arg)

    private fun argDescriptor(
        param: Function.Parameter,
        matcher: ArgMatcher<*>
    ) = ArgumentRenderDescriptor.Matcher(param, matcher)
}
