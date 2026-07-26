package dev.mokkery.internal.interceptor

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.interceptor.MokkeryCallHooks
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.context.ContextCallInterceptor
import dev.mokkery.internal.context.ContextInstantiationListener
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.tracing.callTracing

internal val rootCallInterceptor = ContextCallInterceptor(
    BeforeTracingHookInterceptor,
    CallTracingInterceptor,
    BeforeAnsweringHookInterceptor,
    AnsweringInterceptor
)

internal val rootInstantiationListener = ContextInstantiationListener(
    MocksRegisteringListener
)

internal object MocksRegisteringListener : MokkeryInstantiationListener {

    override fun onInstantiation(scope: MokkeryInstanceScope, mock: Any) {
        scope.mokkeryContext[MokkeryInstancesRegistry]?.register(scope)
    }
}

internal object BeforeTracingHookInterceptor : MokkeryCallInterceptor {
    @DelicateMokkeryApi
    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        val hooks = scope.mokkeryContext[MokkeryCallHooks] ?: return scope.nextIntercept()
        return hooks.internal.beforeTracing.intercept(scope)
    }

    @DelicateMokkeryApi
    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        val hooks = scope.mokkeryContext[MokkeryCallHooks] ?: return scope.nextIntercept()
        return hooks.internal.beforeTracing.intercept(scope)
    }
}

internal object CallTracingInterceptor : MokkeryCallInterceptor {

    @DelicateMokkeryApi
    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        scope.callTracing.trace(scope)
        return scope.nextIntercept()
    }

    @DelicateMokkeryApi
    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        scope.callTracing.trace(scope)
        return scope.nextIntercept()
    }
}

internal object BeforeAnsweringHookInterceptor : MokkeryCallInterceptor {

    @DelicateMokkeryApi
    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        val hooks = scope.mokkeryContext[MokkeryCallHooks] ?: return scope.nextIntercept()
        return hooks.internal.beforeAnswering.intercept(scope)
    }

    @DelicateMokkeryApi
    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        val hooks = scope.mokkeryContext[MokkeryCallHooks] ?: return scope.nextIntercept()
        return hooks.internal.beforeAnswering.intercept(scope)
    }
}

internal object AnsweringInterceptor : MokkeryCallInterceptor {

    @DelicateMokkeryApi
    override fun intercept(scope: MokkeryBlockingCallScope): Any? = scope
        .answering
        .resolveAnswer(scope)
        .call(scope)

    @DelicateMokkeryApi
    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? = scope
        .answering
        .resolveAnswer(scope)
        .call(scope)
}
