package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.answering.Awaitable

@Poko
internal class AwaitAnswer<T>(
    private val awaitable: Awaitable<T>
): Answer.Suspending<T> {

    override suspend fun callSuspend(scope: FunctionScope): T = awaitable.await(scope)

    override fun description(): String = "awaits ${awaitable.description()}"
}