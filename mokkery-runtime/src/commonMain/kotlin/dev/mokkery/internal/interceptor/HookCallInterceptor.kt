package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.MokkeryHook
import dev.mokkery.interceptor.MokkerySuspendCallScope
import kotlinx.atomicfu.atomic

internal class HookCallInterceptor : MokkeryCallInterceptor, MokkeryHook<MokkeryCallInterceptor> {

    private var interceptors by atomic(listOf<MokkeryCallInterceptor>())

    override fun register(interceptor: MokkeryCallInterceptor) {
        interceptors += interceptor
    }

    override fun unregister(interceptor: MokkeryCallInterceptor) {
        interceptors -= interceptor
    }

    override fun intercept(scope: MokkeryBlockingCallScope) = scope
        .combinedInterceptorOf(interceptors)
        .intercept(scope)

    override suspend fun intercept(scope: MokkerySuspendCallScope) = scope
        .combinedInterceptorOf(interceptors)
        .intercept(scope)

    private fun MokkeryCallScope.combinedInterceptorOf(
        interceptors: List<MokkeryCallInterceptor>
    ) = MockInterceptor(interceptors + mockInterceptor)
}
