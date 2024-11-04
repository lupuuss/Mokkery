package dev.mokkery.internal.utils

import dev.mokkery.internal.IllegalSuspensionException
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn

internal fun runSuspension(block: suspend () -> Unit) {
    if (block.startCoroutineUninterceptedOrReturn(EmptyContinuation) == COROUTINE_SUSPENDED) {
        throw IllegalSuspensionException()
    }
}

private val EmptyContinuation = Continuation<Unit>(EmptyCoroutineContext) { }
