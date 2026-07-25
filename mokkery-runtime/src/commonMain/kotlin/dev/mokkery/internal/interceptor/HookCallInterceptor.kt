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

internal class HookCallInterceptor(
    private val parent: HookCallInterceptor? = null,
    interceptors: List<MokkeryCallInterceptor> = emptyList(),
) : MokkeryCallInterceptor, MokkeryHook<MokkeryCallInterceptor> {

    private val localInterceptors = atomic(interceptors)
    private val allInterceptors: List<MokkeryCallInterceptor>
        get() {
            val fromParent = parent?.allInterceptors.orEmpty()
            val local = localInterceptors.value
            return when {
                fromParent.isEmpty() -> local
                local.isEmpty() -> fromParent
                else -> fromParent + local
            }
        }

    fun fork(): HookCallInterceptor = HookCallInterceptor(this)

    override fun register(interceptor: MokkeryCallInterceptor) {
        localInterceptors.update { it + interceptor }
    }

    override fun unregister(interceptor: MokkeryCallInterceptor) {
        localInterceptors.update { it - interceptor }
    }

    override fun intercept(scope: MokkeryBlockingCallScope) = scope
        .combinedInterceptorOf(allInterceptors)
        .intercept(scope)

    override suspend fun intercept(scope: MokkerySuspendCallScope) = scope
        .combinedInterceptorOf(allInterceptors)
        .intercept(scope)

    private fun MokkeryCallScope.combinedInterceptorOf(
        interceptors: List<MokkeryCallInterceptor>
    ) = when {
        interceptors.isEmpty() -> callInterceptor
        else -> ContextCallInterceptor(interceptors + callInterceptor)
    }

    override fun toString(): String = "HookCallInterceptor(parent=$parent, interceptors=${localInterceptors.value})"
}
