package dev.mokkery.internal.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.interceptor.call
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.interceptor.toFunctionScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.calls.callTracing
import dev.mokkery.internal.calls.templating
import dev.mokkery.internal.context.tools

internal enum class MokkeryKind {
    Spy, Mock
}

@Suppress("unused")
internal fun mokkeryMockInterceptor(): MokkeryCallInterceptor = combine(
    TemplatingInterceptor,
    CallTracingInterceptor,
    MokkeryCallHooks.beforeAnswering,
    AnsweringInterceptor
)

private object CallTracingInterceptor : MokkeryCallInterceptor {

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

private object AnsweringInterceptor : MokkeryCallInterceptor {

    @DelicateMokkeryApi
    override fun intercept(scope: MokkeryBlockingCallScope): Any? = scope
        .answering
        .resolveAnswer(scope)
        .call(scope.toFunctionScope())

    @DelicateMokkeryApi
    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? = scope
        .answering
        .resolveAnswer(scope)
        .callSuspend(scope.toFunctionScope())
}

private object TemplatingInterceptor : MokkeryCallInterceptor {

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
