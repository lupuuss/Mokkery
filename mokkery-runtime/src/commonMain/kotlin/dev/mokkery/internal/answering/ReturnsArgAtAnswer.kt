package dev.mokkery.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.utils.unsafeCast

@Poko
internal class ReturnsArgAtAnswer<T>(val index: Int) : Answer<T> {

    override fun call(scope: FunctionScope): T = scope.args[index].unsafeCast()

    override fun description(): String = "returnsArgAt $index"
}
