package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.internal.answer.AnsweringInterceptor
import dev.mokkery.internal.templating.TemplatingInterceptor
import dev.mokkery.internal.tracing.CallTraceClock
import dev.mokkery.internal.tracing.CallTracingInterceptor

internal interface MokkeryMock : MokkerySpy {
    val answering: AnsweringInterceptor
}

internal fun MokkeryMock(receiver: String, mockMode: MockMode): MokkeryMock {
    return MokkeryMockImpl(
        TemplatingInterceptor(receiver),
        CallTracingInterceptor(receiver, CallTraceClock.current),
        AnsweringInterceptor(receiver, mockMode)
    )
}

private class MokkeryMockImpl(
    override val templating: TemplatingInterceptor,
    override val callTracing: CallTracingInterceptor,
    override val answering: AnsweringInterceptor,
) : MokkeryMock, MokkeryInterceptor by combine(templating, callTracing, answering)
