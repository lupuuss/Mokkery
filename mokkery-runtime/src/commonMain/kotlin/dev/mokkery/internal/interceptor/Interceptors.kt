package dev.mokkery.internal.interceptor

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.tracing.callTracing

internal object MocksRegisteringListener : MokkeryInstantiationListener {

    override fun onInstantiation(scope: MokkeryInstanceScope, mock: Any) {
        scope.mokkeryContext[MokkeryInstancesRegistry]?.register(scope)
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
