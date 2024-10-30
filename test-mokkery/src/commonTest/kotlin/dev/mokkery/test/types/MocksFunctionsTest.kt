package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksFunctionsTest {

    @Test
    fun testRegularFunctionWithPrimitive() {
        val func = mock<(Int) -> Double> { every { invoke(any()) } returns 1.0 }
        assertEquals(1.0, func(1))
        verify { func(1) }
    }

    @Test
    fun testRegularFunctionWithComplex() {
        val func = mock<(ComplexType) -> ComplexType> { every { invoke(any()) } returns ComplexType }
        assertEquals(ComplexType, func(ComplexType))
        verify { func(ComplexType) }
    }

    @Test
    fun testSuspendFunctionWithPrimitive() = runTest {
        val func = mock<suspend (Int) -> Double> { everySuspend { invoke(any()) } returns 1.0 }
        assertEquals(1.0, func(1))
        verifySuspend { func(1) }
    }

    @Test
    fun testSuspendFunctionWithComplex() = runTest {
        val func = mock<suspend (ComplexType) -> ComplexType> { everySuspend { invoke(any()) } returns ComplexType }
        assertEquals(ComplexType, func(ComplexType))
        verifySuspend { func(ComplexType) }
    }

    @Test
    fun testRegularFunctionWithPrimitiveExtension() {
        val func = mock<Int.() -> Double> { every { invoke(any()) } returns 1.0 }
        assertEquals(1.0, func(1))
        verify { func(1) }
    }


    @Test
    fun testRegularFunctionWithComplexExtension() {
        val func = mock<ComplexType.() -> ComplexType> { every { invoke(any()) } returns ComplexType }
        assertEquals(ComplexType, func(ComplexType))
        verify { func(ComplexType) }
    }

    @Test
    fun testSuspendFunctionWithPrimitiveExtension() = runTest {
        val func = mock<suspend Int.() -> Double> { everySuspend { invoke(any()) } returns 1.0 }
        assertEquals(1.0, func(1))
        verifySuspend { func(1) }
    }

    @Test
    fun testSuspendFunctionWithComplexExtension() = runTest {
        val func = mock<suspend ComplexType.() -> ComplexType> { everySuspend { invoke(any()) } returns ComplexType }
        assertEquals(ComplexType, func(ComplexType))
        verifySuspend { func(ComplexType) }
    }

    @Test
    fun testRegularFunctionCreatedWithTypeParameters() {
        val func = mockFun<Int, ComplexType>()
        every { func(1) } returns ComplexType
        assertEquals(ComplexType, func(1))
        verify { func(1) }
    }

    @Test
    fun testSuspendFunctionCreatedWithTypeParameters() = runTest {
        val func = mockSusFun<Int, ComplexType>()
        everySuspend { func(1) } returns ComplexType
        assertEquals(ComplexType, func(1))
        verifySuspend { func(1) }
    }

    @Test
    fun testRegularFunctionCreatedWithReifiedTypeParameters() {
        val func = reifiedMockFunR<Int, ComplexType>()
        every { func(1) } returns ComplexType
        assertEquals(ComplexType, func(1))
        verify { func(1) }
    }

    @Test
    fun testSuspendFunctionCreatedWithReifiedTypeParameters() = runTest {
        val func = reifiedMockSusFun<Int, ComplexType>()
        everySuspend { func(1) } returns ComplexType
        assertEquals(ComplexType, func(1))
        verifySuspend { func(1) }
    }

    private fun <T, R> mockFun() = mock<(T) -> R>()

    private fun <T, R> mockSusFun() = mock<suspend (T) -> R>()

    private inline fun <reified T, reified R> reifiedMockFunR() = mock<(T) -> R>()

    private inline fun <reified T, reified R> reifiedMockSusFun() = mock<suspend (T) -> R>()
}
