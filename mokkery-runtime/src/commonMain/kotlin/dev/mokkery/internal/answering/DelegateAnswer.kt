package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.unsafeCast

internal class DelegateAnswer(private val delegate: Any) : Answer<Any?> {
    override fun call(scope: FunctionScope): Any? = delegate
        .unsafeCast<(List<Any?>) -> Any?>()
        .invoke(scope.args)

    override suspend fun callSuspend(scope: FunctionScope): Any? = delegate
        .unsafeCast<suspend (List<Any?>) -> Any?>()
        .invoke(scope.args)
}