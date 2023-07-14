package dev.mokkery.internal

import dev.mokkery.internal.templating.TemplatingInterceptor
import dev.mokkery.internal.tracing.CallTraceClock
import dev.mokkery.internal.tracing.CallTracingInterceptor

internal interface MokkerySpy : MokkeryInterceptor {
    val templating: TemplatingInterceptor
    val callTracing: CallTracingInterceptor
}

@Suppress("unused")
internal fun MokkerySpy(): MokkerySpy = MokkerySpyImpl(
    TemplatingInterceptor(),
    CallTracingInterceptor(CallTraceClock.current)
)

private class MokkerySpyImpl(
    override val templating: TemplatingInterceptor,
    override val callTracing: CallTracingInterceptor,
) : MokkerySpy, MokkeryInterceptor by combine(templating, callTracing)
