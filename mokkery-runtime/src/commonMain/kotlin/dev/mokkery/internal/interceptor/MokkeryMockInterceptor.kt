package dev.mokkery.internal.interceptor

import dev.mokkery.MockMode
import dev.mokkery.interceptor.MokkeryCallInterceptor

internal enum class MokkeryKind {
    Spy, Mock
}

internal interface MokkeryMockInterceptor : MokkeryCallInterceptor {
    val mode: MockMode
    val kind: MokkeryKind

    val templating: TemplatingInterceptor
    val callTracing: CallTracingInterceptor
    val answering: AnsweringInterceptor
}

@Suppress("unused")
internal fun MokkeryMockInterceptor(mockMode: MockMode, kind: MokkeryKind): MokkeryMockInterceptor = MokkeryMockInterceptorImpl(
    mockMode,
    kind,
    TemplatingInterceptor(),
    CallTracingInterceptor(),
    AnsweringInterceptor(mockMode)
)

private class MokkeryMockInterceptorImpl(
    override val mode: MockMode,
    override val kind: MokkeryKind,
    override val templating: TemplatingInterceptor,
    override val callTracing: CallTracingInterceptor,
    override val answering: AnsweringInterceptor,
) : MokkeryMockInterceptor, MokkeryCallInterceptor by combine(templating, callTracing, answering)
