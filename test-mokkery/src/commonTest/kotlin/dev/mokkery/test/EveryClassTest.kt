package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class EveryClassTest {

    private val testClass = mock<TestClass>()

    @Test
    fun testMocksRegularMethod() {
        every { testClass.call() } returns "123"
        assertEquals("123", testClass.call())
    }

    @Test
    fun testMocksBaseClassMethod() {
        every { testClass.baseClassMethod() } returns Unit
        testClass.baseClassMethod()
    }

    @Test
    fun testMocksBaseInterfaceMethod() {
        every { testClass.baseInterfaceMethod() } returns Unit
        testClass.baseInterfaceMethod()
    }

    @Test
    fun testMocksBaseClassProperty() {
        every { testClass.baseClassProperty } returns "123"
        assertEquals("123", testClass.baseClassProperty)
    }

    @Test
    fun testMocksBaseInterfaceProperty() {
        every { testClass.baseInterfaceProperty } returns "321"
        assertEquals("321", testClass.baseInterfaceProperty)
    }
}
