package dev.mokkery.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.Answer
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.call
import dev.mokkery.internal.utils.unsafeCast

@Poko
internal class ReturnsArgAtAnswer<T>(val index: Int) : Answer.Unified<T> {

    override fun call(scope: MokkeryCallScope): T = scope.call.args[index].value.unsafeCast()

    override fun description(): String = "returnsArgAt $index"
}
