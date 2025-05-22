package dev.mokkery.test

import kotlin.test.assertFailsWith

internal inline fun assertVerified(crossinline block: () -> Unit) {
    assertFailsWith<AssertionError> {
        block()
    }.also { println(it.message) }
}
