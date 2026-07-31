package dev.mokkery.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkeryCallScope
import dev.mokkery.answering.Answer
import dev.mokkery.call
import dev.mokkery.internal.ArgIndexOutOfBoundsException
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable

@Poko
internal class ReturnsArgAtAnswer<T>(val index: Int) : Answer.Unified<T>, Renderable {

    override fun call(scope: MokkeryCallScope): T {
        val args = scope.call.args
        val arg = args.getOrNull(index) ?: throw ArgIndexOutOfBoundsException(index, args.size)
        return arg.value.unsafeCast()
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "returnsArgAt $index"
}
