package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryCallInterceptor

internal enum class MokkeryKind {
    Spy, Mock
}

internal interface MokkeryMockInterceptor : MokkeryCallInterceptor {

    val templating: TemplatingInterceptor
    val callTracing: CallTracingInterceptor
    val answering: AnsweringInterceptor
}

@Suppress("unused")
internal fun MokkeryMockInterceptor(): MokkeryMockInterceptor = MokkeryMockInterceptorImpl(
    TemplatingInterceptor(),
    CallTracingInterceptor(),
    AnsweringInterceptor()
)

private class MokkeryMockInterceptorImpl(
    override val templating: TemplatingInterceptor,
    override val callTracing: CallTracingInterceptor,
    override val answering: AnsweringInterceptor,
) : MokkeryMockInterceptor, MokkeryCallInterceptor by combine(
    templating,
    callTracing,
    MokkeryCallHooks.beforeAnswering,
    answering
)
