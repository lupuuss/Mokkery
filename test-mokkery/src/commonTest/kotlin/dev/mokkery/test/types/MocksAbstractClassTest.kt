package dev.mokkery.test.types

import dev.mokkery.answering.SuperCall.Companion.original
import dev.mokkery.answering.SuperCall.Companion.superOf
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.AbstractClassLevel1
import dev.mokkery.test.ComplexType
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksAbstractClassTest {

    private val mock = mock<AbstractClassLevel1>()

    @Test
    fun testCallPrimitive() {
        every { mock.callPrimitive(1) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        verify { mock.callPrimitive(1) }
    }

    @Test
    fun testCallComplex() {
        every { mock.callComplex(any()) } returns ComplexType
        assertEquals(ComplexType, mock.callComplex(ComplexType))
        verify { mock.callComplex(ComplexType) }
    }

    @Test
    fun testCallPrimitiveOriginal() {
        every { mock.callPrimitive(any()) } calls original
        assertEquals(4, mock.callPrimitive(4))
        verify { mock.callPrimitive(4) }
    }

    @Test
    fun testCallPrimitiveSuper() {
        every { mock.callPrimitive(any()) } calls superOf<AbstractClassLevel1>()
        assertEquals(4, mock.callPrimitive(4))
        verify { mock.callPrimitive(4) }
    }

    @Test
    fun testIgnoredMembers() {
        assertEquals("Ignored inline property", mock.inlineProperty)
        assertEquals("Ignored inline method", mock.inlineMethod())
        assertEquals("Ignored final method", mock.finalMethod())
    }
}
