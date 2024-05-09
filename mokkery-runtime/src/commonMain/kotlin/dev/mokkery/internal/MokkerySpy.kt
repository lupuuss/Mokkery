package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.templating.TemplatingInterceptor
import dev.mokkery.internal.tracing.CallTracingInterceptor

internal interface MokkerySpy : MokkeryInterceptor {
    val templating: TemplatingInterceptor
    val callTracing: CallTracingInterceptor
    val answering: AnsweringInterceptor
}

@Suppress("unused")
internal fun MokkerySpy(): MokkerySpy = MokkerySpyImpl(
    TemplatingInterceptor(),
    CallTracingInterceptor(),
    AnsweringInterceptor(MockMode.strict)
)

private class MokkerySpyImpl(
    override val templating: TemplatingInterceptor,
    override val callTracing: CallTracingInterceptor,
    override val answering: AnsweringInterceptor,
) : MokkerySpy, MokkeryInterceptor by combine(templating, callTracing, answering)
