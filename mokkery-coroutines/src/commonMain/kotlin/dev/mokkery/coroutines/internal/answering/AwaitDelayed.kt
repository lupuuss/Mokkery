package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import kotlinx.coroutines.delay
import kotlin.time.Duration

@Poko
internal class AwaitDelayed<T>(
    private val duration: Duration,
    private val valueDescription: MokkeryRenderingScope.() -> String,
    private val value: suspend (MokkerySuspendCallScope) -> T,
) : Awaitable<T>, Renderable {

    override suspend fun await(scope: MokkerySuspendCallScope): T {
        delay(duration)
        return value(scope)
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "delayed(by=$duration, ${valueDescription.invoke(scope)})"
}
