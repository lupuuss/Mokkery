package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.templating.TemplatingInterceptor
import dev.mokkery.internal.tracing.CallTracingInterceptor

internal enum class MokkeryKind {
    Spy, Mock
}

internal interface MokkeryMock : MokkeryInterceptor {
    val mode: MockMode
    val kind: MokkeryKind

    val templating: TemplatingInterceptor
    val callTracing: CallTracingInterceptor
    val answering: AnsweringInterceptor
}

@Suppress("unused")
internal fun MokkeryMock(mockMode: MockMode, kind: MokkeryKind): MokkeryMock = MokkeryMockImpl(
    mockMode,
    kind,
    TemplatingInterceptor(),
    CallTracingInterceptor(Counter.calls),
    AnsweringInterceptor(mockMode)
)

private class MokkeryMockImpl(
    override val mode: MockMode,
    override val kind: MokkeryKind,
    override val templating: TemplatingInterceptor,
    override val callTracing: CallTracingInterceptor,
    override val answering: AnsweringInterceptor,
) : MokkeryMock, MokkeryInterceptor by combine(templating, callTracing, answering)
