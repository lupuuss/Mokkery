package dev.mokkery.internal.coroutines

import kotlinx.coroutines.runBlocking

internal actual fun runSuspension(block: suspend () -> Unit) {
    runBlocking {
        block()
    }
}
