package dev.mokkery.test

import dev.mokkery.internal.CallContext
import dev.mokkery.internal.MokkeryInterceptor

internal open class TestMokkeryInterceptor : MokkeryInterceptor {

    private val _interceptedCalls = mutableListOf<CallContext>()
    private val _interceptedSuspendCalls = mutableListOf<CallContext>()

    var interceptCallResult: Any? = null
    var interceptCallSuspendResult: Any? = null

    override fun interceptCall(context: CallContext): Any? {
        _interceptedCalls.add(context)
        return interceptCallResult
    }

    override suspend fun interceptSuspendCall(context: CallContext): Any? {
        _interceptedSuspendCalls.add(context)
        return interceptCallSuspendResult
    }
}
