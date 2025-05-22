package dev.mokkery.test

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkerySuspendCallScope

internal open class TestMokkeryCallInterceptor(
    var interceptBlock: (MokkeryBlockingCallScope) -> Any? = { null },
    var interceptSuspendBlock: suspend (MokkerySuspendCallScope) -> Any? = { null },
) : MokkeryCallInterceptor {

    private val _interceptedCalls = mutableListOf<MokkeryBlockingCallScope>()
    private val _interceptedSuspendCalls = mutableListOf<MokkerySuspendCallScope>()

    val interceptedCalls: List<MokkeryCallScope> = _interceptedCalls
    val interceptedSuspendCalls: List<MokkeryCallScope> = _interceptedSuspendCalls

    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        _interceptedCalls.add(scope)
        return interceptBlock(scope)
    }

    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        _interceptedSuspendCalls.add(scope)
        return interceptSuspendBlock(scope)
    }
}
