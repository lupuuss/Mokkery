package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryCallHooks
import dev.mokkery.internal.mokkeryRuntimeError

internal fun MokkeryCallHooks.fork(): MokkeryCallHooksImpl {
    val hooks = this as? MokkeryCallHooksImpl ?: mokkeryRuntimeError("Custom `MokkeryCallHooks` implementations are not supported!")
    return hooks.fork()
}

internal class MokkeryCallHooksImpl(
    override val beforeTracing: HookCallInterceptor = HookCallInterceptor(),
    override val beforeAnswering: HookCallInterceptor = HookCallInterceptor(),
) : MokkeryCallHooks {

    fun fork(): MokkeryCallHooksImpl = MokkeryCallHooksImpl(
        beforeTracing = beforeTracing.fork(),
        beforeAnswering = beforeAnswering.fork(),
    )
}
