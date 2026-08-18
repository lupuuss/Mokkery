@file:Suppress("unused")

package dev.mokkery.internal.dispatcher

import dev.mokkery.internal.mokkeryRuntimeError

@PublishedApi
internal class LambdaSpyCallDispatcher(
    private val blocking: ((List<Any?>) -> Any?)?,
    private val suspending: (suspend (List<Any?>) -> Any?)?,
) : SpyCallDispatcher {

    override fun mokkeryDispatchSpyCall(memberId: Int, args: List<Any?>): Any? {
        val lambda = blocking ?: mokkeryRuntimeError("Blocking spy call dispatched to a suspend lambda mock!")
        return lambda(args)
    }

    override suspend fun mokkeryDispatchSpyCallSuspend(memberId: Int, args: List<Any?>): Any? {
        val lambda = suspending ?: mokkeryRuntimeError("Suspend spy call dispatched to a blocking lambda mock!")
        return lambda(args)
    }
}
