package dev.mokkery.internal.coroutines

internal actual fun runSuspension(block: suspend () -> Unit) = runSkippingSuspension(block)
