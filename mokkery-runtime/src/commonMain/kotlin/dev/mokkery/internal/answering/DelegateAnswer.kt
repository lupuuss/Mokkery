package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.context.argValues
import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.call
import dev.mokkery.internal.utils.unsafeCast

internal class DelegateAnswer(private val delegate: Function<Any?>) : Answer<Any?> {
    override fun call(scope: MokkeryBlockingCallScope): Any? = delegate
        .unsafeCast<(List<Any?>) -> Any?>()
        .invoke(scope.call.argValues)

    override suspend fun call(scope: MokkerySuspendCallScope): Any? = delegate
        .unsafeCast<suspend (List<Any?>) -> Any?>()
        .invoke(scope.call.argValues)
}
