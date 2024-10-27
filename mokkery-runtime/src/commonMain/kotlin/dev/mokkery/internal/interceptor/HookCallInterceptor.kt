package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.MokkeryHook
import kotlinx.atomicfu.atomic

internal class HookCallInterceptor : MokkeryCallInterceptor, MokkeryHook<MokkeryCallInterceptor> {

    private var interceptors by atomic(listOf<MokkeryCallInterceptor>())

    override fun register(interceptor: MokkeryCallInterceptor) {
        interceptors += interceptor
    }

    override fun unregister(interceptor: MokkeryCallInterceptor) {
        interceptors -= interceptor
    }

    override fun intercept(scope: MokkeryCallScope) = scope
        .combinedInterceptorOf(interceptors)
        .intercept(scope)

    override suspend fun interceptSuspend(scope: MokkeryCallScope) = scope
        .combinedInterceptorOf(interceptors)
        .interceptSuspend(scope)

    private fun MokkeryCallScope.combinedInterceptorOf(
        interceptors: List<MokkeryCallInterceptor>
    ) = (interceptors + context.nextInterceptor).combined()
}
