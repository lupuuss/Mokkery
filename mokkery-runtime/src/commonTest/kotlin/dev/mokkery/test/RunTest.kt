package dev.mokkery.test

import dev.mokkery.internal.utils.runSuspension

@Suppress("NOTHING_TO_INLINE")
internal inline fun runTest(noinline block: suspend () -> Unit) = runSuspension(block)
