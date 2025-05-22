package dev.mokkery.test

import dev.mokkery.MokkeryCallScope
import dev.mokkery.internal.context.ContextCallInterceptor

internal class TestContextCallInterceptor(
    interceptBlock: (MokkeryCallScope) -> Any? = { null },
    interceptSuspendBlock: suspend (MokkeryCallScope) -> Any? = { null }
) : TestMokkeryCallInterceptor(interceptBlock, interceptSuspendBlock), ContextCallInterceptor
