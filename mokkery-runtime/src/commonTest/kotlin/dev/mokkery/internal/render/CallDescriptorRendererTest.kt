package dev.mokkery.internal.render

import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeFunParam
import kotlin.test.Test
import kotlin.test.assertEquals

class CallDescriptorRendererTest {

    private val valueRenderer = TestRenderer<Any?> { it.toString() }
    private val matcherRenderer = TestRenderer<ArgMatcher<*>> { it.toString() }
    private val instanceIdRenderer = TestRenderer<MokkeryInstanceId> { it.toString() }

    private fun renderer(renderReceiver: Boolean = true): Renderer<CallRenderDescriptor> {
        return Renderers.default.callDescriptor(
            renderReceiver = renderReceiver,
            valueRenderer = valueRenderer,
            matcherRenderer = matcherRenderer,
            instanceIdRenderer = instanceIdRenderer,
        )
    }

    @Test
    fun testRenderGetterWithReceiverOffForCallArguments() {
        renderer(renderReceiver = false)
            .assertRendered(
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
        renderer(renderReceiver = true)
            .assertRendered(
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
        renderer(renderReceiver = false)
            .assertRendered(
                expected = "foo",
                descriptor = testDescriptor(getterDescriptor("foo")),
            )
    }

    @Test
    fun testRenderGetterWithReceiverOnForNoArguments() {
        renderer(renderReceiver = true)
            .assertRendered(
                expected = "Mock(1).foo",
                descriptor = testDescriptor(getterDescriptor("foo")),
            )
    }

    @Test
    fun testRenderGetterWithReceiverOffForMatchers() {
        renderer(renderReceiver = false)
            .assertRendered(
                expected = "(<receiver_1> = any(), <receiver_2> = any()).foo",
                descriptor = testDescriptor(
                    getterDescriptor("foo"),
                    argDescriptor(fakeFunParam<Int>(name = "<receiver_1>"), ArgMatcher.Any),
                    argDescriptor(fakeFunParam<Int>(name = "<receiver_2>"), ArgMatcher.Any),
                ),
            )
    }

    @Test
    fun testRenderGetterWithReceiverOnForMatchers() {
        renderer(renderReceiver = true)
            .assertRendered(
                expected = "Mock(1).(<receiver_1> = any(), <receiver_2> = any()).foo",
                descriptor = testDescriptor(
                    getterDescriptor("foo"),
                    argDescriptor(fakeFunParam<Int>(name = "<receiver_1>"), ArgMatcher.Any),
                    argDescriptor(fakeFunParam<Int>(name = "<receiver_2>"), ArgMatcher.Any),
                ),
            )
    }

    @Test
    fun testRenderSetterWithReceiverOffForCallArguments() {
        renderer(renderReceiver = false)
            .assertRendered(
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
        renderer(renderReceiver = true)
            .assertRendered(
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
        renderer(renderReceiver = false)
            .assertRendered(
                expected = "foo = 5",
                descriptor = testDescriptor(
                    setterDescriptor("foo"),
                    argDescriptor(fakeCallArg(value = 5, name = "value"))
                ),
            )
    }

    @Test
    fun testRenderSetterWithReceiverOnForForSingleArgument() {
        renderer(renderReceiver = true)
            .assertRendered(
                expected = "Mock(1).foo = 5",
                descriptor = testDescriptor(
                    setterDescriptor("foo"),
                    argDescriptor(fakeCallArg(value = 5, name = "value"))
                ),
            )
    }

    @Test
    fun testRenderSetterWithReceiverOffForSingleMatcher() {
        renderer(renderReceiver = false)
            .assertRendered(
                expected = "foo = any()",
                descriptor = testDescriptor(
                    setterDescriptor("foo"),
                    argDescriptor(fakeFunParam<Int>(name = "value"), ArgMatcher.Any),
                ),
            )
    }

    @Test
    fun testRenderSetterWithReceiverOnForForSingleMatcher() {
        renderer(renderReceiver = true)
            .assertRendered(
                expected = "Mock(1).foo = any()",
                descriptor = testDescriptor(
                    setterDescriptor("foo"),
                    argDescriptor(fakeFunParam<Int>(name = "value"), ArgMatcher.Any),
                ),
            )
    }

    @Test
    fun testRenderSetterWithReceiverOffForMatchers() {
        renderer(renderReceiver = false)
            .assertRendered(
                expected = "(<receiver_1> = any(), <receiver_2> = any()).foo = any()",
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
        renderer(renderReceiver = true)
            .assertRendered(
                expected = "Mock(1).(<receiver_1> = any(), <receiver_2> = any()).foo = any()",
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
        renderer(renderReceiver = false)
            .assertRendered(
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
        renderer(renderReceiver = true)
            .assertRendered(
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
        renderer(renderReceiver = false)
            .assertRendered(
                expected = "foo()",
                descriptor = testDescriptor(funcDescriptor("foo")),
            )
    }

    @Test
    fun testRenderFunctionWithReceiverOnForNoArguments() {
        renderer(renderReceiver = true)
            .assertRendered(
                expected = "Mock(1).foo()",
                descriptor = testDescriptor(funcDescriptor("foo")),
            )
    }

    @Test
    fun testRenderFunctionWithReceiverOffForMatchers() {
        renderer(renderReceiver = false)
            .assertRendered(
                expected = "foo(p1 = any(), p2 = any())",
                descriptor = testDescriptor(
                    funcDescriptor("foo"),
                    argDescriptor(fakeFunParam<Int>(name = "p1"), ArgMatcher.Any),
                    argDescriptor(fakeFunParam<Int>(name = "p2"), ArgMatcher.Any),
                ),
            )
    }

    @Test
    fun testRenderFunctionWithReceiverOnForMatchers() {
        renderer(renderReceiver = true)
            .assertRendered(
                expected = "Mock(1).foo(p1 = any(), p2 = any())",
                descriptor = testDescriptor(
                    funcDescriptor("foo"),
                    argDescriptor(fakeFunParam<Int>(name = "p1"), ArgMatcher.Any),
                    argDescriptor(fakeFunParam<Int>(name = "p2"), ArgMatcher.Any),
                ),
            )
    }


    private fun Renderer<CallRenderDescriptor>.assertRendered(
        expected: String,
        descriptor: CallRenderDescriptor
    ) = assertEquals(expected, render(descriptor))

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
