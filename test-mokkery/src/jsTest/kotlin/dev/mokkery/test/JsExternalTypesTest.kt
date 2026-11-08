package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test
import kotlin.test.assertEquals

class JsExternalTypesTest {

    private val mock = mock<JsExternalInterface>()

    @Test
    fun testJsonParameter() {
        every { mock.callJson(any()) } returns 1
        assertEquals(1, mock.callJson(json("a" to 1)))
    }

    @Test
    fun testPromiseReturnType() {
        val promise = Promise.resolve(1)
        every { mock.producePromise() } returns promise
        assertEquals(promise, mock.producePromise())
    }

    @Test
    fun testExternalInterfaceParameter() {
        every { mock.callExternal(any()) } returns 2
        assertEquals(2, mock.callExternal(js("({})")))
    }

    @Test
    fun testGenericMockWithExternalTypeArgument() {
        val generic = mock<GenericExternalInterface<JsExternalShape>>()
        every { generic.call(any()) } returns 3
        assertEquals(3, generic.call(js("({})")))
    }
}

external interface JsExternalShape

interface GenericExternalInterface<T> {
    fun call(value: T): Int
}

interface JsExternalInterface {
    fun callJson(value: Json): Int
    fun producePromise(): Promise<Int>
    fun callExternal(value: JsExternalShape): Int
}
