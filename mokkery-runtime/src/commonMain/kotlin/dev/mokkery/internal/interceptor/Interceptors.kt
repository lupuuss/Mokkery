package dev.mokkery.internal.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.call
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.calls.callTracing
import dev.mokkery.internal.calls.templating
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.context.tools

internal object MocksRegisteringListener : MokkeryInstantiationListener {

    override fun onInstantiation(scope: MokkeryInstanceScope, mock: Any) {
        scope.mokkeryContext[MocksRegistry]?.register(scope)
    }
}

internal object TemplatingInterceptor : MokkeryCallInterceptor {

    @DelicateMokkeryApi
    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        val templating = scope.templating
        if (!templating.isEnabled) return scope.nextIntercept()
        val hint = templating.currentGenericHint
        templating.saveTemplate(scope)
        return scope.tools.autofillProvider.provideValue(hint ?: scope.call.function.returnType)
    }

    @DelicateMokkeryApi
    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        val templating = scope.templating
        if (!templating.isEnabled) return scope.nextIntercept()
        val hint = templating.currentGenericHint
        templating.saveTemplate(scope)
        return scope.tools.autofillProvider.provideValue(hint ?: scope.call.function.returnType)
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
