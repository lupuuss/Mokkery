package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.answering.Awaitable
import kotlinx.coroutines.delay
import kotlin.time.Duration

@Poko
internal class AwaitDelayed<T>(
    private val duration: Duration,
    private val valueDescription: () -> String,
    private val value: suspend (FunctionScope) -> T,
) : Awaitable<T> {

    override suspend fun await(scope: FunctionScope): T {
        delay(duration)
        return value(scope)
    }

    override fun description(): String = "delayed(by=$duration, ${valueDescription.invoke()})"
}
