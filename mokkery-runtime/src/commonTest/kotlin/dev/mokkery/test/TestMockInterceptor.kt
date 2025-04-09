package dev.mokkery.test

import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.internal.interceptor.MockInterceptor

internal class TestMockInterceptor(
    interceptBlock: (MokkeryCallScope) -> Any? = { null },
    interceptSuspendBlock: suspend (MokkeryCallScope) -> Any? = { null }
) : TestMokkeryCallInterceptor(interceptBlock, interceptSuspendBlock), MockInterceptor
