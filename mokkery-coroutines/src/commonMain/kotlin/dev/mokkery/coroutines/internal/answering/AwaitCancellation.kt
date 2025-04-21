package dev.mokkery.coroutines.internal.answering

import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.MokkerySuspendCallScope
import kotlinx.coroutines.awaitCancellation

internal data object AwaitCancellation : Awaitable<Nothing> {

    override suspend fun await(scope: MokkerySuspendCallScope): Nothing = awaitCancellation()

    override fun description(): String = "cancellation"
}
