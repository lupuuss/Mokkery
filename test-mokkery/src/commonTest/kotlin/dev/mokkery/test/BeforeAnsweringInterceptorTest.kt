package dev.mokkery.test

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.interceptor.call
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(DelicateMokkeryApi::class)
class BeforeAnsweringInterceptorTest {

    private val interceptor = TestInterceptor(
        interceptBlock = { it.nextIntercept() },
        interceptSuspendBlock = { it.nextIntercept() }
    )

    @BeforeTest
    fun before() {
        MokkeryCallInterceptor.beforeAnswering.register(interceptor)
    }

    @AfterTest
    fun after() {
        MokkeryCallInterceptor.beforeAnswering.unregister(interceptor)
    }

    @Test
    fun testCallsInterceptWithBlockingMethodsWhenRegistered() {
        val mock = mock<TestInterface> {
            every { callWithPrimitives(1) } returns 1.0
        }
        mock.callWithPrimitives(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callWithPrimitives" }
        )
        assertTrue(
            interceptor
                .interceptSuspendCalls
                .isEmpty()
        )
    }

    @Test
    fun testCallsInterceptWithSuspendMethodsWhenRegistered() = runTest {
        val mock = mock<TestInterface> {
            everySuspend { callWithSuspension(1) } returns listOf()
        }
        mock.callWithSuspension(1)
        assertNotNull(
            interceptor
                .interceptSuspendCalls
                .singleOrNull { it.call.function.name == "callWithSuspension" }
        )
        assertTrue(
            interceptor
                .interceptBlockingCalls
                .isEmpty()
        )
    }

    @Test
    fun testDoesNotCallInterceptorWhenUnregistered() = runTest {
        MokkeryCallInterceptor.beforeAnswering.unregister(interceptor)
        val mock = mock<TestInterface> {
            every { callWithPrimitives(1) } returns 1.0
            everySuspend { callWithSuspension(1) } returns listOf()
        }
        mock.callWithPrimitives(1)
        mock.callWithSuspension(1)
        assertTrue(
            interceptor
                .interceptBlockingCalls
                .isEmpty()
        )
        assertTrue(
            interceptor
                .interceptSuspendCalls
                .isEmpty()
        )
    }

    @Test
    fun testAllowsProvidingReturnValueForBlockingMethods() {
        val mock = mock<TestInterface>()
        interceptor.interceptBlock = { 33.0 }
        assertEquals(33.0, mock.callWithPrimitives(1))
    }

    @Test
    fun testAllowsProvidingReturnValueForSuspendMethods() = runTest {
        val mock = mock<TestInterface>()
        interceptor.interceptSuspendBlock = { listOf("Hello!") }
        assertEquals(listOf("Hello!"), mock.callWithSuspension(1))
    }
}

@OptIn(DelicateMokkeryApi::class)
class TestInterceptor(
    var interceptBlock: (MokkeryBlockingCallScope) -> Any?,
    var interceptSuspendBlock: suspend (MokkerySuspendCallScope) -> Any?,
) : MokkeryCallInterceptor {

    private val interceptCalls = mutableListOf<MokkeryCallScope>()

    val interceptBlockingCalls get() = interceptCalls.filterIsInstance<MokkeryBlockingCallScope>()
    val interceptSuspendCalls get() = interceptCalls.filterIsInstance<MokkerySuspendCallScope>()

    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        interceptCalls += scope
        return interceptBlock(scope)
    }

    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        interceptCalls += scope
        return interceptSuspendBlock(scope)
    }

}
