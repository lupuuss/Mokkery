package dev.mokkery.internal.answering

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.Answer
import dev.mokkery.answering.BlockingCallDefinitionScope
import dev.mokkery.answering.CallArgs
import dev.mokkery.answering.SuspendCallDefinitionScope
import dev.mokkery.context.argValues
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.interceptor.call

internal class CallsCatchingAnswer<T>(
    private val block: BlockingCallDefinitionScope<Result<T>>.(CallArgs) -> T
) : Answer.Blocking<Result<T>> {

    override fun call(scope: MokkeryBlockingCallScope) = runCatchingSkippingMokkery {
        block(BlockingCallDefinitionScope(scope), CallArgs(scope.call.argValues))
    }

    override fun description() = "callsCatching {...}"
}

internal class CallsCatchingSuspendAnswer<T>(
    private val block: suspend SuspendCallDefinitionScope<Result<T>>.(CallArgs) -> T
) : Answer.Suspending<Result<T>> {

    override suspend fun call(scope: MokkerySuspendCallScope): Result<T> = runCatchingSkippingMokkery {
        block(SuspendCallDefinitionScope(scope), CallArgs(scope.call.argValues))
    }

    override fun description() = "callsCatching {...}"
}

private inline fun <T> runCatchingSkippingMokkery(block: () -> T) = runCatching { block() }
    .onFailure { if (it is MokkeryRuntimeException) throw it }
