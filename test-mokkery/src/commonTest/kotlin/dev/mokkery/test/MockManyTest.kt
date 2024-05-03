package dev.mokkery.test

import dev.mokkery.answering.SuperCall.Companion.superOf
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mockMany
import dev.mokkery.t1
import dev.mokkery.t2
import dev.mokkery.t3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockManyTest {

    private val mock = mockMany<A, B, C> {
        every { t1.methodA(any()) } calls { (input: Int) -> "methodA($input)" }
        every { t2.methodB(any()) } calls { (input: Int) -> "methodB($input)" }
        every { t3.methodC(any()) } calls { (input: Int) -> "methodC($input)" }
    }

    @Test
    fun testMockImplementsMultipleTypes() {
        assertIs<A>(mock)
        assertIs<B>(mock)
        assertIs<C>(mock)
    }

    @Test
    fun testMocksRegularMethodsFromMultipleInterfaces() {
        assertEquals("methodA(1)", mock.t1.methodA(1))
        assertEquals("methodB(2)", mock.t2.methodB(2))
        assertEquals("methodC(3)", mock.t3.methodC(3))
    }

    @Test
    fun testMocksNameCollidingMethodsAsSingleMethod() {
        every { mock.t1.method(any()) } returns "Hello!"
        assertEquals("Hello!", mock.t1.method(1))
        assertEquals("Hello!", mock.t2.method(2))
        assertEquals("Hello!", mock.t3.method(3))
    }

    @Test
    fun testMocksNameCollidingPropertiesAsSingleProperty() {
        every { mock.t1.property } returns 10
        assertEquals(10, mock.t1.property)
        assertEquals(10, mock.t2.property)
        assertEquals(10, mock.t3.property)
    }

    @Test
    fun testMocksProperlyResolvesNameCollidingSuperCallsToMethods() {
        every { mock.t1.method(1) } calls superOf<A>()
        every { mock.t1.method(2) } calls superOf<B>()
        every { mock.t1.method(3) } calls superOf<C>()
        assertEquals("method(1) from A", mock.t1.method(1))
        assertEquals("method(2) from B", mock.t1.method(2))
        assertEquals("Overridden method(3) from C", mock.t1.method(3))
    }

    @Test
    fun testMocksProperlyResolvesNameCollidingSuperCallsToProperties() {
        every { mock.t1.property } calls superOf<A>()
        assertEquals(1, mock.t1.property)
        every { mock.t1.property } calls superOf<B>()
        assertEquals(2, mock.t1.property)
        every { mock.t1.property } calls superOf<C>()
        assertEquals(3, mock.t1.property)
    }

    private interface A {

        var property: Int
            get() = 1
            set(value) {}

        fun method(input: Int): String = "method($input) from A"

        fun methodA(input: Int): String
    }

    private abstract class B {

        open var property: Int
            get() = 2
            set(value) {}

        open fun method(input: Int): String = "method($input) from B"

        abstract fun methodB(input: Int): String
    }

    private interface C : A {

        override var property: Int
            get() = 3
            set(value) {}

        override fun method(input: Int): String = "Overridden method($input) from C"

        fun methodC(input: Int): String
    }
}