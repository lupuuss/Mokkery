package dev.mokkery.test

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal inline fun assertVerified(crossinline block: () -> Unit) {
    assertFailsWith<AssertionError> {
        block()
    }.also { println(it.message) }
}


internal inline fun assertVerifiedWith(message: String, crossinline block: () -> Unit) {
    assertFailsWith<AssertionError> {
        block()
    }.also {
        println(it.message)
        assertEquals(message, it.message)
    }
}
