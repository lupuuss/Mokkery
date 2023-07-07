package dev.mokkery.test

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.anyVarargs
import dev.mokkery.matcher.eq
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EveryTest {

    private val dependencyMock = mock<TestDependency>()

    @Test
    fun testMocksRegularMethodCallWithPrimitiveTypes() {
        every { dependencyMock.callWithPrimitives(any(), any()) } returns 1.0
        assertEquals(1.0, dependencyMock.callWithPrimitives(1))
    }


    @Test
    fun testMocksRegularMethodCallWithComplexTypes() {
        every { dependencyMock.callWithComplex(any()) } returns listOf(1, 2, 3)
        assertEquals(listOf(1, 2, 3), dependencyMock.callWithComplex(listOf()))
    }

    @Test
    fun testMocksRegularMethodWithExtensionReceiver() {
        every { dependencyMock.run { any<Int>().callWithExtensionReceiver() } } calls { (i: Int) -> i.toString() }
        assertEquals("1", dependencyMock.run { 1.callWithExtensionReceiver() })
    }

    @Test
    fun testMockMethodsWithAnyVarargs() {
        every { dependencyMock.callWithVararg(any(), *anyVarargs()) } returns (1.0 to 1.0)
        assertEquals(1.0 to 1.0, dependencyMock.callWithVararg(1, "2", "3"))
        assertEquals(1.0 to 1.0, dependencyMock.callWithVararg(1, "2"))
        assertEquals(1.0 to 1.0, dependencyMock.callWithVararg(1))
    }

    @Test
    fun testMockMethodsWithAnyMixedVarargs() {
        every { dependencyMock.callWithVararg(any(), "1", *anyVarargs(), eq("3")) } returns (1.0 to 1.0)
        assertEquals(1.0 to 1.0, dependencyMock.callWithVararg(1, "1", "2", "2", "3"))
        assertEquals(1.0 to 1.0, dependencyMock.callWithVararg(1, "1", "2", "3"))
        assertEquals(1.0 to 1.0, dependencyMock.callWithVararg(1, "1", "3"))
    }

    @Test
    fun testMocksSuspendingMethods() = runTest {
        everySuspend { dependencyMock.callWithSuspension(any()) } returns listOf("1", "2")
        assertEquals(listOf("1", "2"), dependencyMock.callWithSuspension(1))
    }

    @Test
    fun testMocksMethodsWithNothingReturnType() {
        every { dependencyMock.callNothing() } throws IllegalArgumentException("FAILED!")
        assertFailsWith<IllegalArgumentException> { dependencyMock.callNothing() }
    }

    @Test
    fun testMocksPropertyGetter() {
        every { dependencyMock.property } returns "1234"
        assertEquals("1234", dependencyMock.property)
    }

    @Test
    fun testMocksPropertySetter() {
        var capture: String? = null
        every { dependencyMock.property = any() } calls { (value: String) -> capture = value }
        dependencyMock.property = "1234"
        assertEquals("1234", capture)
    }

    @Test
    fun testMockSupportsNamedParametersWithMixedLiteralsAndMatchers() {
        every { dependencyMock.callWithPrimitives(j = 2, i = any()) } returns 2.0
        assertEquals(2.0, dependencyMock.callWithPrimitives(3, 2))
    }

    @Test
    fun testMockAnswersAreResolvedInReversedOrder() {
        every { dependencyMock.callWithPrimitives(any(), any()) } returns 1.0
        every { dependencyMock.callWithPrimitives(1, 1) } returns 2.0
        assertEquals(2.0, dependencyMock.callWithPrimitives(1, 1))
    }
}
