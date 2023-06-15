package dev.mokkery.internal.coroutines

internal expect fun runSuspension(block: suspend () -> Unit)

