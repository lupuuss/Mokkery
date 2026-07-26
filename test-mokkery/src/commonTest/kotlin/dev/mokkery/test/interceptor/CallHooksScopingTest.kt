package dev.mokkery.test.interceptor

import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.call
import dev.mokkery.every
import dev.mokkery.interceptor.callHooks
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.RegularMethodsInterface
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CallHooksScopingTest {

    @Test
    fun testGlobalInterceptorAppliesToStandaloneMock() {
        val interceptor = TestInterceptor()
        MokkeryScope.global.callHooks.beforeAnswering.register(interceptor)
        try {
            val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
            mock.callPrimitive(1)
            assertNotNull(
                interceptor
                    .interceptBlockingCalls
                    .singleOrNull { it.call.function.name == "callPrimitive" }
            )
        } finally {
            MokkeryScope.global.callHooks.beforeAnswering.unregister(interceptor)
        }
    }

    @Test
    fun testGlobalInterceptorAppliesToSuiteScopedMocks() {
        val interceptor = TestInterceptor()
        MokkeryScope.global.callHooks.beforeAnswering.register(interceptor)
        try {
            val scope = MokkerySuiteScope()
            val mock = scope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
            mock.callPrimitive(1)
            assertNotNull(
                interceptor
                    .interceptBlockingCalls
                    .singleOrNull { it.call.function.name == "callPrimitive" }
            )
        } finally {
            MokkeryScope.global.callHooks.beforeAnswering.unregister(interceptor)
        }
    }

    @Test
    fun testSuiteScopedInterceptorAppliesToMocksOfThatScope() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        scope.callHooks.beforeAnswering.register(interceptor)
        val mock = scope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testSuiteScopedInterceptorDoesNotApplyToMocksOfOtherScope() {
        val interceptor = TestInterceptor()
        val registeredScope = MokkerySuiteScope()
        val otherScope = MokkerySuiteScope()
        registeredScope.callHooks.beforeAnswering.register(interceptor)
        val otherMock = otherScope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        otherMock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testInstanceScopedInterceptorAppliesToThatMock() {
        val interceptor = TestInterceptor()
        val mock = mock<RegularMethodsInterface> {
            callHooks.beforeAnswering.register(interceptor)
            every { callPrimitive(any()) } returnsArgAt 0
        }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testInstanceScopedInterceptorRegisteredAfterCreationAppliesToThatMock() {
        val interceptor = TestInterceptor()
        val mock = mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        MokkeryScope
            .from(mock)
            .callHooks
            .beforeAnswering
            .register(interceptor)
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testInstanceScopedInterceptorDoesNotApplyToOtherMock() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        scope.mock<RegularMethodsInterface> {
            callHooks.beforeAnswering.register(interceptor)
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val otherMock = scope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        otherMock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testSuiteScopedBeforeTracingInterceptorAppliesToMocksOfThatScope() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        scope.callHooks.beforeTracing.register(interceptor)
        val mock = scope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testSuiteScopedBeforeTracingInterceptorDoesNotApplyToMocksOfOtherScope() {
        val interceptor = TestInterceptor()
        val registeredScope = MokkerySuiteScope()
        val otherScope = MokkerySuiteScope()
        registeredScope.callHooks.beforeTracing.register(interceptor)
        val otherMock = otherScope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        otherMock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testInstanceScopedBeforeTracingInterceptorAppliesToThatMock() {
        val interceptor = TestInterceptor()
        val mock = mock<RegularMethodsInterface> {
            callHooks.beforeTracing.register(interceptor)
            every { callPrimitive(any()) } returnsArgAt 0
        }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testInstanceScopedBeforeTracingInterceptorDoesNotApplyToOtherMock() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        scope.mock<RegularMethodsInterface> {
            callHooks.beforeTracing.register(interceptor)
            every { callPrimitive(any()) } returnsArgAt 0
        }
        val otherMock = scope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        otherMock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }
}
