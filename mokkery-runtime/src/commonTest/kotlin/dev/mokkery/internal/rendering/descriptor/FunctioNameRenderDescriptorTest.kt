package dev.mokkery.internal.rendering.descriptor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertIsNot

class FunctioNameRenderDescriptorTest {

    @Test
    fun testGetter() {
        val descriptor = assertIs<GetterRenderDescriptor>(FunctionRenderDescriptor.parse("<get-foo>"))
        assertIsNot<SetterRenderDescriptor>(descriptor)
        assertEquals("foo", descriptor.name)
    }

    @Test
    fun testSetter() {
        val descriptor = assertIs<SetterRenderDescriptor>(FunctionRenderDescriptor.parse("<set-foo>"))
        assertIsNot<GetterRenderDescriptor>(descriptor)
        assertEquals("foo", descriptor.name)
    }

    @Test
    fun testRegularFun() {
        val descriptor = FunctionRenderDescriptor.parse("call")
        assertIsNot<SetterRenderDescriptor>(descriptor)
        assertIsNot<GetterRenderDescriptor>(descriptor)
        assertEquals("call", descriptor.name)
    }
}
