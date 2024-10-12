package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.templating.TemplatingInterceptor
import dev.mokkery.internal.tracing.CallTracingInterceptor

internal enum class MokkeryKind {
    Spy, Mock
}

internal interface MokkeryMockInterceptor : MokkeryInterceptor {
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
    CallTracingInterceptor(Counter.calls),
    AnsweringInterceptor(mockMode)
)

private class MokkeryMockInterceptorImpl(
    override val mode: MockMode,
    override val kind: MokkeryKind,
    override val templating: TemplatingInterceptor,
    override val callTracing: CallTracingInterceptor,
    override val answering: AnsweringInterceptor,
) : MokkeryMockInterceptor, MokkeryInterceptor by combine(templating, callTracing, answering)
