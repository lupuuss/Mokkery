package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.MokkerySuspendCallScope
import kotlinx.coroutines.delay
import kotlin.time.Duration

@Poko
internal class AwaitDelayed<T>(
    private val duration: Duration,
    private val valueDescription: () -> String,
    private val value: suspend (MokkerySuspendCallScope) -> T,
) : Awaitable<T> {

    override suspend fun await(scope: MokkerySuspendCallScope): T {
        delay(duration)
        return value(scope)
    }

    override fun description(): String = "delayed(by=$duration, ${valueDescription.invoke()})"
}
