package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import org.junit.Test
import kotlin.test.assertEquals

class ValueClassTest {

    private val mock = mock<TestInterface>()

    @Test
    fun testMocksMethodsWithPrimitiveResultReturnType() {
        every { mock.callWithPrimitiveResult(any()) } returns Result.success(1)
        assertEquals(Result.success(1), mock.callWithPrimitiveResult(Result.success(0)))
    }

    @Test
    fun testMocksMethodsWithComplexResultReturnType() {
        every { mock.callWithComplexResult(any()) } returns Result.success(listOf(1))
        assertEquals(Result.success(listOf(1)), mock.callWithComplexResult(Result.success(emptyList())))
    }
}
