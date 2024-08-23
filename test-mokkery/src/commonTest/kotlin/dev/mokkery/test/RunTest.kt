package dev.mokkery.test

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
fun runTest(block: suspend () -> Unit) = dev.mokkery.internal.coroutines.runSuspension(block)
