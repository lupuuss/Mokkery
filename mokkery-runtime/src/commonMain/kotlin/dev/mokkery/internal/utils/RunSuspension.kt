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

internal fun runSuspensionNothing(block: suspend () -> Nothing): Nothing {
    if (block.startCoroutineUninterceptedOrReturn(EmptyContinuation) == COROUTINE_SUSPENDED) {
        throw IllegalSuspensionException()
    }
    error("Given lambda should fail, but it didn't!")
}

private val EmptyContinuation = Continuation<Unit>(EmptyCoroutineContext) { }
