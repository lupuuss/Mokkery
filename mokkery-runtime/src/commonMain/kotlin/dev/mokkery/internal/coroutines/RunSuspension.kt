package dev.mokkery.internal.coroutines

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn

internal fun runSuspension(block: suspend () -> Unit) {
    if (block.startCoroutineUninterceptedOrReturn(EmptyContinuation) == COROUTINE_SUSPENDED) {
        error("`everySuspend`/`verifySuspend` does not support actual suspension! Only mock method calls are allowed!")
    }
}

private val EmptyContinuation = Continuation<Unit>(EmptyCoroutineContext) { }
