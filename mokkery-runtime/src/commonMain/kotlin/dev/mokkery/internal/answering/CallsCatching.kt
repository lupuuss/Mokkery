package dev.mokkery.internal.answering

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.answering.Answer
import dev.mokkery.answering.BlockingCallDefinitionScope
import dev.mokkery.answering.CallArgs
import dev.mokkery.answering.SuspendCallDefinitionScope
import dev.mokkery.call
import dev.mokkery.context.argValues
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable

internal class CallsCatchingAnswer<T>(
    private val block: BlockingCallDefinitionScope<Result<T>>.(CallArgs) -> T
) : Answer.Blocking<Result<T>>, Renderable {

    override fun call(scope: MokkeryBlockingCallScope) = runCatchingSkippingMokkery {
        block(BlockingCallDefinitionScope(scope), CallArgs(scope.call.argValues))
    }

    context(scope: MokkeryRenderingScope)
    override fun render() = "callsCatching {...}"
}

internal class CallsCatchingSuspendAnswer<T>(
    private val block: suspend SuspendCallDefinitionScope<Result<T>>.(CallArgs) -> T
) : Answer.Suspending<Result<T>>, Renderable {

    override suspend fun call(scope: MokkerySuspendCallScope): Result<T> = runCatchingSkippingMokkery {
        block(SuspendCallDefinitionScope(scope), CallArgs(scope.call.argValues))
    }

    context(scope: MokkeryRenderingScope)
    override fun render() = "callsCatching {...}"
}

private inline fun <T> runCatchingSkippingMokkery(block: () -> T) = runCatching { block() }
    .onFailure { if (it is MokkeryRuntimeException) throw it }
