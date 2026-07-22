package dev.mokkery.test.interceptor

import dev.mokkery.MokkeryScope
import dev.mokkery.answering.returns
import dev.mokkery.call
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.interceptor.callHooks
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.test.SuspendMethodsInterface
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BeforeTracingInterceptorTest {

    private val interceptor = TestInterceptor()

    @BeforeTest
    fun before() {
        MokkeryScope.global.callHooks.beforeTracing.register(interceptor)
    }

    @AfterTest
    fun after() {
        MokkeryScope.global.callHooks.beforeTracing.unregister(interceptor)
    }

    @Test
    fun testCallsInterceptWithBlockingMethodsWhenRegistered() {
        val mock = mock<RegularMethodsInterface> {
            every { callPrimitive(1) } returns 1
        }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
        assertTrue(
            interceptor
                .interceptSuspendCalls
                .isEmpty()
        )
    }

    @Test
    fun testCallsInterceptWithSuspendMethodsWhenRegistered() = runTest {
        val mock = mock<SuspendMethodsInterface> { everySuspend { callComplex(ComplexType.Companion) } returns ComplexType.Companion }
        mock.callComplex(ComplexType.Companion)
        assertNotNull(
            interceptor
                .interceptSuspendCalls
                .singleOrNull { it.call.function.name == "callComplex" }
        )
        assertTrue(
            interceptor
                .interceptBlockingCalls
                .isEmpty()
        )
    }

    @Test
    fun testDoesNotCallInterceptorWhenUnregistered() = runTest {
        MokkeryScope.global.callHooks.beforeTracing.unregister(interceptor)
        val mockA = mock<RegularMethodsInterface> { every { callPrimitive(1) } returns 1 }
        val mockB = mock<SuspendMethodsInterface> { everySuspend { callComplex(ComplexType.Companion) } returns ComplexType.Companion }
        mockA.callPrimitive(1)
        mockB.callComplex(ComplexType.Companion)
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
        val mock = mock<RegularMethodsInterface>()
        interceptor.interceptBlock = { 33 }
        assertEquals(33, mock.callPrimitive(1))
    }

    @Test
    fun testAllowsProvidingReturnValueForSuspendMethods() = runTest {
        val mock = mock<SuspendMethodsInterface>()
        interceptor.interceptSuspendBlock = { ComplexType.Companion }
        assertEquals(ComplexType.Companion, mock.callComplex(ComplexType.Companion))
    }

    @Test
    fun testRunsBeforeCallIsTraced() {
        val mock = mock<RegularMethodsInterface>()
        interceptor.interceptBlock = { 42 }
        assertEquals(42, mock.callPrimitive(1))
        verifyNoMoreCalls(mock)
    }
}
