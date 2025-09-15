package dev.mokkery.test.types

import dev.mokkery.answering.SuperCall.Companion.original
import dev.mokkery.answering.SuperCall.Companion.superOf
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.templating.ext
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.DefaultsInterfaceLevel1
import dev.mokkery.test.DefaultsInterfaceLevel2
import dev.mokkery.test.ignoreOnJs
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksInterfaceWithDefaultsTest {

    private val mock = mock<DefaultsInterfaceLevel1<String>>()

    @Test
    fun testProperty() {
        every { mock.property } returns "1"
        every { mock.property = any() } returns Unit
        assertEquals("1", mock.property)
        mock.property = "2"
        verify {
            mock.property
            mock.property = "2"
        }
    }

    @Test
    fun testPropertyDefault() {
        every { mock.property } calls original
        every { mock.property = any() } calls original
        assertEquals(null, mock.property)
        mock.property = "Hello"
        verify {
            mock.property
            mock.property = "Hello"
        }
    }

    @Test
    fun testExtProperty() {
        every { mock.ext { any<Int>().extProperty } } returns 1.0
        every { mock.ext { any<Int>().extProperty = any<Double>() } } returns Unit
        assertEquals(1.0, mock.run { 1.extProperty })
        mock.run { 2.extProperty = 2.0 }
        verify {
            mock.ext {
                1.extProperty
                2.extProperty = 2.0
            }
        }
    }

    @Test
    fun testExtPropertyDefault() = ignoreOnJs("Calling supers of extension functions from IR is not supported on JS") {
        every { mock.ext { any<Int>().extProperty } } calls original
        every { mock.ext { any<Int>().extProperty = any<Double>() } } calls original
        assertEquals(1.0, mock.run { 1.extProperty })
        mock.run { 1.extProperty = 2.0 }
        every { mock.ext { any<Int>().extProperty } } calls superOf<DefaultsInterfaceLevel1<*>>()
        assertEquals(1.0, mock.run { 1.extProperty })
        verify {
            mock.ext {
                1.extProperty
                1.extProperty = 2.0
            }
        }
    }

    @Test
    fun testExtPropertyGeneric() {
        every { mock.ext { any<String>().extPropertyGeneric } } returns "1"
        every { mock.ext { any<String>().extPropertyGeneric = any<String>() } } returns Unit
        assertEquals("1", mock.run { "1".extPropertyGeneric })
        mock.run { "2".extPropertyGeneric = "2" }
        verify {
            mock.ext {
                "1".extPropertyGeneric
                "2".extPropertyGeneric = "2"
            }
        }
    }

    @Test
    fun testExtPropertyGenericDefaults() =
        ignoreOnJs("Calling supers of extension functions from IR is not supported on JS") {
            every { mock.ext { any<String>().extPropertyGeneric } } calls original
            every { mock.ext { any<String>().extPropertyGeneric = any<String>() } } calls original
            assertEquals("1", mock.run { "1".extPropertyGeneric })
            mock.run { "2".extPropertyGeneric = "2" }
            verify {
                mock.ext {
                    "1".extPropertyGeneric
                    "2".extPropertyGeneric = "2"
                }
            }
        }

    @Test
    fun testCall() {
        every { mock.call(any(), any()) } returns ComplexType("a")
        assertEquals(ComplexType("a"), mock.call(1, ComplexType))
        verify { mock.call(1, ComplexType) }
    }


    @Test
    fun testCallDefaults() {
        every { mock.call(any(), any()) } calls original
        assertEquals(ComplexType("1"), mock.call(1, ComplexType))
        every { mock.call(any(), any()) } calls superOf<DefaultsInterfaceLevel1<*>>()
        assertEquals(ComplexType("1"), mock.call(1, ComplexType))
        verify { mock.call(1, ComplexType) }
    }

    @Test
    fun testCallSuspend() = runTest {
        everySuspend { mock.callSuspend(any(), any()) } returns ComplexType("a")
        assertEquals(ComplexType("a"), mock.callSuspend(1, ComplexType))
        verifySuspend { mock.callSuspend(1, ComplexType) }
    }

    @Test
    fun testCallSuspendDefaults() = runTest {
        everySuspend { mock.callSuspend(any(), any()) } calls original
        assertEquals(ComplexType("1"), mock.callSuspend(1, ComplexType))
        everySuspend { mock.callSuspend(any(), any()) } calls superOf<DefaultsInterfaceLevel1<*>>()
        assertEquals(ComplexType("1"), mock.callSuspend(1, ComplexType))
        verifySuspend { mock.callSuspend(1, ComplexType) }
    }

    @Test
    fun testCallExtension() = runTest {
        everySuspend { mock.ext { any<Int>().callExtension(any()) } } returns ComplexType("a")
        assertEquals(ComplexType("a"), mock.run { 1.callExtension(ComplexType) })
        verifySuspend { mock.ext { 1.callExtension(ComplexType) } }
    }

    @Test
    fun testCallExtensionDefaults() = runTest {
        ignoreOnJs("Calling supers of extension functions from IR is not supported on JS") {
            everySuspend { mock.ext { any<Int>().callExtension(any()) } } calls original
            assertEquals(ComplexType("1"), mock.run { 1.callExtension(ComplexType) })
            everySuspend { mock.ext { any<Int>().callExtension(any()) } } calls superOf<DefaultsInterfaceLevel1<*>>()
            assertEquals(ComplexType("1"), mock.run { 1.callExtension(ComplexType) })
            verifySuspend { mock.ext { 1.callExtension(ComplexType) } }
        }
    }
}
