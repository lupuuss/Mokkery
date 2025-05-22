package dev.mokkery.test.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.interceptor.nextIntercept

@OptIn(DelicateMokkeryApi::class)
class TestInterceptor(
    var interceptBlock: (MokkeryBlockingCallScope) -> Any? = { it.nextIntercept() },
    var interceptSuspendBlock: suspend (MokkerySuspendCallScope) -> Any? = { it.nextIntercept() },
) : MokkeryCallInterceptor {

    private val interceptCalls = mutableListOf<MokkeryCallScope>()

    val interceptBlockingCalls get() = interceptCalls.filterIsInstance<MokkeryBlockingCallScope>()
    val interceptSuspendCalls get() = interceptCalls.filterIsInstance<MokkerySuspendCallScope>()

    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        interceptCalls += scope
        return interceptBlock(scope)
    }

    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        interceptCalls += scope
        return interceptSuspendBlock(scope)
    }
}
