package dev.mokkery.test

import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope

internal open class TestMokkeryCallInterceptor(
    var interceptBlock: (MokkeryCallScope) -> Any? = { null },
    var interceptSuspendBlock: suspend (MokkeryCallScope) -> Any? = { null },
) : MokkeryCallInterceptor {

    private val _interceptedCalls = mutableListOf<MokkeryCallScope>()
    private val _interceptedSuspendCalls = mutableListOf<MokkeryCallScope>()

    val interceptedCalls: List<MokkeryCallScope> = _interceptedCalls
    val interceptedSuspendCalls: List<MokkeryCallScope> = _interceptedSuspendCalls

    override fun intercept(scope: MokkeryCallScope): Any? {
        _interceptedCalls.add(scope)
        return interceptBlock(scope)
    }

    override suspend fun interceptSuspend(scope: MokkeryCallScope): Any? {
        _interceptedSuspendCalls.add(scope)
        return interceptSuspendBlock(scope)
    }
}
