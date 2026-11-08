package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class JsInteropTypesTest {

    private val mock = mock<JsInteropInterface>()

    @Test
    fun testJsAnyParameter() {
        every { mock.callAny(any()) } returns 1
        assertEquals(1, mock.callAny("value".toJsString()))
    }

    @Test
    fun testJsStringReturnType() {
        val value = "value".toJsString()
        every { mock.produceString() } returns value
        assertEquals(value, mock.produceString())
    }

    @Test
    fun testJsReferenceParameter() {
        every { mock.callReference(any()) } returns 2
        assertEquals(2, mock.callReference(Any().toJsReference()))
    }

    @Test
    fun testExternalTypeParameter() {
        every { mock.callExternal(any()) } returns 3
        assertEquals(3, mock.callExternal(createExternal()))
    }

    @Test
    fun testJsArrayParameter() {
        every { mock.callArray(any()) } returns 4
        assertEquals(4, mock.callArray(JsArray()))
    }
}

external interface ExternalShape : JsAny

private fun createExternal(): ExternalShape = js("({})")

interface JsInteropInterface {
    fun callAny(value: JsAny): Int
    fun produceString(): JsString
    fun callReference(value: JsReference<Any>): Int
    fun callExternal(value: ExternalShape): Int
    fun callArray(value: JsArray<JsString>): Int
}
