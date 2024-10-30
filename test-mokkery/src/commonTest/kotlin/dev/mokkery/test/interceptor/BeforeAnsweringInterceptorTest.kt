package dev.mokkery.test.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.call
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.test.SuspendMethodsInterface
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
        MokkeryCallInterceptor.beforeAnswering.unregister(interceptor)
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
}
