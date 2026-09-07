@file:OptIn(DelicateMokkeryApi::class)

package dev.mokkery.test.interceptor

import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.call
import dev.mokkery.every
import dev.mokkery.factory.create
import dev.mokkery.factory.mockFactoryOf
import dev.mokkery.factory.spyFactoryOf
import dev.mokkery.interceptor.callHooks
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.test.ComplexType
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.test.SpyTestInterface
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

    @Test
    fun testMockFactoryScopedInterceptorAppliesToMocksOfThatFactory() {
        val interceptor = TestInterceptor()
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            callHooks.beforeAnswering.register(interceptor)
        }
        val mock = factory.create<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testMockFactoryScopedInterceptorDoesNotApplyToMocksOfOtherFactory() {
        val interceptor = TestInterceptor()
        mockFactoryOf(RegularMethodsInterface::class) { callHooks.beforeAnswering.register(interceptor) }
        val otherFactory = mockFactoryOf(RegularMethodsInterface::class)
        val otherMock = otherFactory.create<RegularMethodsInterface> {
            every { callPrimitive(any()) } returnsArgAt 0
        }
        otherMock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testMockFactoryScopedInterceptorAppliesToMocksOfCopiedFactory() {
        val interceptor = TestInterceptor()
        val factory = mockFactoryOf(RegularMethodsInterface::class) {
            callHooks.beforeAnswering.register(interceptor)
        }
        val mock = factory
            .copy()
            .create<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testSuiteScopedInterceptorAppliesToMocksOfFactoryFromThatScope() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        scope.callHooks.beforeAnswering.register(interceptor)
        val factory = scope.mockFactoryOf(RegularMethodsInterface::class)
        val mock = factory.create<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testSuiteScopedInterceptorDoesNotApplyToMocksOfFactoryFromOtherScope() {
        val interceptor = TestInterceptor()
        val registeredScope = MokkerySuiteScope()
        val otherScope = MokkerySuiteScope()
        registeredScope.callHooks.beforeAnswering.register(interceptor)
        val factory = otherScope.mockFactoryOf(RegularMethodsInterface::class)
        val mock = factory.create<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        mock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testSpyFactoryScopedInterceptorAppliesToSpiesOfThatFactory() {
        val interceptor = TestInterceptor()
        val factory = spyFactoryOf(SpyTestInterface::class) {
            callHooks.beforeAnswering.register(interceptor)
        }
        val spy = factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        spy.call(ComplexType.Companion("1"))
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "call" }
        )
    }

    @Test
    fun testSpyFactoryScopedInterceptorDoesNotApplyToSpiesOfOtherFactory() {
        val interceptor = TestInterceptor()
        spyFactoryOf(SpyTestInterface::class) { callHooks.beforeAnswering.register(interceptor) }
        val otherFactory = spyFactoryOf(SpyTestInterface::class)
        val otherSpy = otherFactory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        otherSpy.call(ComplexType.Companion("1"))
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testSpyFactoryScopedInterceptorAppliesToSpiesOfCopiedFactory() {
        val interceptor = TestInterceptor()
        val factory = spyFactoryOf(SpyTestInterface::class) {
            callHooks.beforeAnswering.register(interceptor)
        }
        val spy = factory
            .copy()
            .create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        spy.call(ComplexType.Companion("1"))
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "call" }
        )
    }

    @Test
    fun testSuiteScopedInterceptorAppliesToSpiesOfFactoryFromThatScope() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        scope.callHooks.beforeAnswering.register(interceptor)
        val factory = scope.spyFactoryOf(SpyTestInterface::class)
        val spy = factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        spy.call(ComplexType.Companion("1"))
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "call" }
        )
    }

    @Test
    fun testSuiteScopedInterceptorDoesNotApplyToSpiesOfFactoryFromOtherScope() {
        val interceptor = TestInterceptor()
        val registeredScope = MokkerySuiteScope()
        val otherScope = MokkerySuiteScope()
        registeredScope.callHooks.beforeAnswering.register(interceptor)
        val factory = otherScope.spyFactoryOf(SpyTestInterface::class)
        val spy = factory.create<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        spy.call(ComplexType.Companion("1"))
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testMockFactoryInterceptorFromCopyWithNewScopeAppliesToMocksOfThatFactory() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        val factory = mockFactoryOf(RegularMethodsInterface::class)
            .copy(scope) { callHooks.beforeAnswering.register(interceptor) }
        val mock = factory.create<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        mock.callPrimitive(1)
        assertNotNull(
            interceptor
                .interceptBlockingCalls
                .singleOrNull { it.call.function.name == "callPrimitive" }
        )
    }

    @Test
    fun testMockFactoryInterceptorFromCopyWithNewScopeDoesNotApplyToMocksOfThatScope() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        mockFactoryOf(RegularMethodsInterface::class)
            .copy(scope) { callHooks.beforeAnswering.register(interceptor) }
        val mock = scope.mock<RegularMethodsInterface> { every { callPrimitive(any()) } returnsArgAt 0 }
        mock.callPrimitive(1)
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }

    @Test
    fun testSpyFactoryInterceptorFromCopyWithNewScopeDoesNotApplyToSpiesOfThatScope() {
        val interceptor = TestInterceptor()
        val scope = MokkerySuiteScope()
        spyFactoryOf(SpyTestInterface::class)
            .copy(scope) { callHooks.beforeAnswering.register(interceptor) }
        val spy = scope.spy<SpyTestInterface<Int>>(SpyTestInterface.Companion())
        spy.call(ComplexType.Companion("1"))
        assertTrue(interceptor.interceptBlockingCalls.isEmpty())
    }
}
