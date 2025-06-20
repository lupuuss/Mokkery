package dev.mokkery.test.types

import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.answering.returns
import dev.mokkery.matcher.any
import dev.mokkery.templating.ext
import dev.mokkery.test.ComplexType
import dev.mokkery.test.PropertiesInterface
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksPropertiesTest {

    private val mock = mock<PropertiesInterface>()

    @Test
    fun testGetPrimitive() {
        every { mock.primitiveProperty } returns 1
        assertEquals(1, mock.primitiveProperty)
        verify { mock.primitiveProperty }
    }

    @Test
    fun testSetPrimitive() {
        every { mock.primitiveProperty = any() } returns Unit
        mock.primitiveProperty = 1
        verify { mock.primitiveProperty = 1 }
    }

    @Test
    fun testGetComplex() {
        every { mock.complexProperty } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.complexProperty)
        verify { mock.complexProperty }
    }

    @Test
    fun testSetComplex() {
        every { mock.complexProperty = any() } returns Unit
        mock.complexProperty = ComplexType.Companion
        verify { mock.complexProperty = ComplexType.Companion }
    }

    @Test
    fun testGetPrimitiveExtension() {
        every { mock.ext { 1.primitivePropertyExtension } } returns 1
        assertEquals(1, mock.run { 1.primitivePropertyExtension })
        verify { mock.ext { 1.primitivePropertyExtension } }
    }

    @Test
    fun testSetPrimitiveExtension() {
        every { mock.ext { 1.primitivePropertyExtension = 1 } } returns Unit
        mock.run { 1.primitivePropertyExtension = 1 }
        verify { mock.ext { 1.primitivePropertyExtension = 1 } }
    }

    @Test
    fun testGetComplexExtension() {
        every { mock.ext { ComplexType.complexPropertyExtension } } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.run { ComplexType.complexPropertyExtension })
        verify { mock.ext { ComplexType.complexPropertyExtension } }
    }

    @Test
    fun testSetComplexExtension() {
        every { mock.ext { ComplexType.complexPropertyExtension = ComplexType.Companion } } returns Unit
        mock.run { ComplexType.complexPropertyExtension = ComplexType.Companion }
        verify { mock.ext { ComplexType.complexPropertyExtension = ComplexType.Companion } }
    }
}
