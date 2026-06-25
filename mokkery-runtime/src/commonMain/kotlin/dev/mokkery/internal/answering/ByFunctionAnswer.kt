package dev.mokkery.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkeryCallScope
import dev.mokkery.answering.Answer
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable

@Poko
internal class ByFunctionAnswer<T>(
    private val description: String,
    private val block: () -> T,
) : Answer.Unified<T>, Renderable {
    override fun call(scope: MokkeryCallScope): T = block()

    @Suppress("OVERRIDE_DEPRECATION")
    override fun description(): String = description

    context(scope: MokkeryRenderingScope)
    override fun render(): String = description
}
