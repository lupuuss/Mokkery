package dev.mokkery.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope

@Poko
internal class ByFunctionAnswer<T>(
    private val description: String,
    private val block: () -> T,
) : Answer<T> {
    override fun call(scope: FunctionScope): T = block()

    override fun description(): String = description
}