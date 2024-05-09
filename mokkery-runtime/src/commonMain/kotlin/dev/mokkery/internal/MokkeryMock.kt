package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.templating.TemplatingInterceptor
import dev.mokkery.internal.tracing.CallTracingInterceptor

internal interface MokkeryMock : MokkerySpy {
    val mode: MockMode
}

@Suppress("unused")
internal fun MokkeryMock(mockMode: MockMode): MokkeryMock = MokkeryMockImpl(
    mockMode,
    TemplatingInterceptor(),
    CallTracingInterceptor(Counter.calls),
    AnsweringInterceptor(mockMode)
)

private class MokkeryMockImpl(
    override val mode: MockMode,
    override val templating: TemplatingInterceptor,
    override val callTracing: CallTracingInterceptor,
    override val answering: AnsweringInterceptor,
) : MokkeryMock, MokkeryInterceptor by combine(templating, callTracing, answering)
