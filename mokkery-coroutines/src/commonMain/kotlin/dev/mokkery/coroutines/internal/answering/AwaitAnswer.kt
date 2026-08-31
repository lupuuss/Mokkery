package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.answering.Answer
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable

@Poko
internal class AwaitAnswer<T>(
    private val awaitable: Awaitable<T>
): Answer.Suspending<T>, Renderable {

    override suspend fun call(scope: MokkerySuspendCallScope): T = awaitable.await(scope)

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "awaits ${awaitable.renderOrDescription()}"
}

@Suppress("DEPRECATION")
context(scope: MokkeryRenderingScope)
private fun Awaitable<*>.renderOrDescription(): String = when (this) {
    is Renderable -> render()
    else -> description()
}
