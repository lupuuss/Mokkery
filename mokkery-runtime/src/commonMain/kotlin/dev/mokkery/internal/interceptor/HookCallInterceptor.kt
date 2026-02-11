package dev.mokkery.internal.interceptor

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryHook
import dev.mokkery.internal.context.ContextCallInterceptor
import dev.mokkery.internal.context.callInterceptor
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

internal class HookCallInterceptor : MokkeryCallInterceptor, MokkeryHook<MokkeryCallInterceptor> {

    private val interceptors = atomic(listOf<MokkeryCallInterceptor>())

    override fun register(interceptor: MokkeryCallInterceptor) {
        interceptors.update { it + interceptor }
    }

    override fun unregister(interceptor: MokkeryCallInterceptor) {
        interceptors.update { it - interceptor }
    }

    override fun intercept(scope: MokkeryBlockingCallScope) = scope
        .combinedInterceptorOf(interceptors.value)
        .intercept(scope)

    override suspend fun intercept(scope: MokkerySuspendCallScope) = scope
        .combinedInterceptorOf(interceptors.value)
        .intercept(scope)

    private fun MokkeryCallScope.combinedInterceptorOf(
        interceptors: List<MokkeryCallInterceptor>
    ) = if (interceptors.isEmpty()) {
        callInterceptor
    } else {
        ContextCallInterceptor(interceptors + callInterceptor)
    }

    override fun toString(): String = "HookCallInterceptor(interceptors=${interceptors.value})"
}
