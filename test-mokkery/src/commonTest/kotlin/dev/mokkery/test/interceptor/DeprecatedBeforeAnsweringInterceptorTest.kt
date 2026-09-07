package dev.mokkery.test.interceptor

import dev.mokkery.MokkeryScope
import dev.mokkery.answering.returns
import dev.mokkery.call
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.callHooks
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.test.SuspendMethodsInterface
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("DEPRECATION")
class DeprecatedBeforeAnsweringInterceptorTest {

    private val interceptor = TestInterceptor()

    @AfterTest
    fun after() {
        MokkeryCallInterceptor.beforeAnswering.unregister(interceptor)
    }

    @Test
    fun testCallsInterceptWithBlockingMethodsWhenRegistered() {
        MokkeryCallInterceptor.beforeAnswering.register(interceptor)
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(1) } returns 1 }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testCallsInterceptWithSuspendMethodsWhenRegistered() = runTest {
        MokkeryCallInterceptor.beforeAnswering.register(interceptor)
        val mock = mock<SuspendMethodsInterface> {
            everySuspend { callComplex(ComplexType.Companion) } returns ComplexType.Companion
        }
        mock.callComplex(ComplexType.Companion)
        assertNotNull(
            interceptor
                .interceptSuspendCalls
                .singleOrNull { it.call.function.name == "callComplex" }
        )
    }

    @Test
    fun testAllowsProvidingReturnValue() {
        MokkeryCallInterceptor.beforeAnswering.register(interceptor)
        val mock = mock<RegularMethodsInterface>()
        interceptor.interceptBlock = { 33 }
        assertEquals(33, mock.callPrimitive(1))
    }

    @Test
    fun testDoesNotCallInterceptorWhenUnregistered() {
        MokkeryCallInterceptor.beforeAnswering.register(interceptor)
        MokkeryCallInterceptor.beforeAnswering.unregister(interceptor)
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(1) } returns 1 }
        mock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testRegistersInGlobalScopeBeforeAnsweringHook() {
        MokkeryCallInterceptor.beforeAnswering.register(interceptor)
        MokkeryScope.global.callHooks.beforeAnswering.unregister(interceptor)
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(1) } returns 1 }
        mock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }
}