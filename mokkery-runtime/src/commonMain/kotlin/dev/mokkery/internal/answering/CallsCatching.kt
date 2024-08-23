package dev.mokkery.internal.answering

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.Answer
import dev.mokkery.answering.BlockingCallDefinitionScope
import dev.mokkery.answering.CallArgs
import dev.mokkery.answering.FunctionScope
import dev.mokkery.answering.SuspendCallDefinitionScope

internal class CallsCatchingAnswer<T>(
    private val block: BlockingCallDefinitionScope<Result<T>>.(CallArgs) -> T
) : Answer<Result<T>> {
    override fun call(scope: FunctionScope) = runCatchingSkippingMokkery {
        block(BlockingCallDefinitionScope(scope), CallArgs(scope.args))
    }

    override fun description() = "callsCatching {...}"
}

internal class CallsCatchingSuspendAnswer<T>(
    private val block: suspend SuspendCallDefinitionScope<Result<T>>.(CallArgs) -> T
) : Answer.Suspending<Result<T>> {

    override suspend fun callSuspend(scope: FunctionScope): Result<T> = runCatchingSkippingMokkery {
        block(SuspendCallDefinitionScope(scope), CallArgs(scope.args))
    }

    override fun description() = "callsCatching {...}"
}

private inline fun <T> runCatchingSkippingMokkery(block: () -> T) = runCatching { block() }
    .onFailure { if (it is MokkeryRuntimeException) throw it }
