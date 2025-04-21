package dev.mokkery.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.Answer
import dev.mokkery.MokkeryCallScope

@Poko
internal class ByFunctionAnswer<T>(
    private val description: String,
    private val block: () -> T,
) : Answer.Unified<T> {
    override fun call(scope: MokkeryCallScope): T = block()

    override fun description(): String = description
}
