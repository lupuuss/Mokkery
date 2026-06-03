package dev.mokkery.test

import dev.mokkery.MokkeryRuntimeException
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

internal inline fun assertMokkeryError(expectedMessage: String, block: () -> Unit) {
    val error = assertFailsWith<MokkeryRuntimeException> { block() }
    assertEquals(expectedMessage, error.message)
    println(error.message)
}
