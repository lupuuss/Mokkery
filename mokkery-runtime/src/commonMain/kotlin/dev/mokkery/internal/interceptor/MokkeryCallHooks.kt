package dev.mokkery.internal.interceptor

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.interceptor.MokkeryCallHooks
import dev.mokkery.internal.mokkeryRuntimeError

internal fun MokkeryScope.forkedHooksOrEmpty(): MokkeryContext {
    val hooks = mokkeryContext[MokkeryCallHooks] ?: return MokkeryContext.Empty
    return hooks.internal.fork()
}

internal val MokkeryCallHooks.internal: ForkedMokkeryCallHooks
    get() = this as? ForkedMokkeryCallHooks ?: mokkeryRuntimeError("Custom `MokkeryCallHooks` implementations are not supported!")

internal class ForkedMokkeryCallHooks(
    override val beforeTracing: HookCallInterceptor = HookCallInterceptor(),
    override val beforeAnswering: HookCallInterceptor = HookCallInterceptor(),
) : MokkeryCallHooks {

    fun fork(): ForkedMokkeryCallHooks = ForkedMokkeryCallHooks(
        beforeTracing = beforeTracing.fork(),
        beforeAnswering = beforeAnswering.fork(),
    )
}
