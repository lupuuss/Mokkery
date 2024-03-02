package dev.mokkery.internal.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

internal actual fun runSuspension(block: suspend () -> Unit) = runSkippingSuspension(block)

internal fun runSkippingSuspension(block: suspend () -> Unit) {
    var exception: Throwable? = null
    SkippingSuspensionScope.launch(start = CoroutineStart.UNDISPATCHED) {
        exception = runCatching { block() }.exceptionOrNull()
    }
    exception?.let { throw it }
}

private val SkippingSuspensionScope = CoroutineScope(SkippingSuspensionDispatcher)

@OptIn(InternalCoroutinesApi::class)
private object SkippingSuspensionDispatcher : CoroutineDispatcher(), Delay {
    override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()

    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: CancellableContinuation<Unit>
    ) = continuation.resume(Unit)

    override fun isDispatchNeeded(context: CoroutineContext) = false

}
