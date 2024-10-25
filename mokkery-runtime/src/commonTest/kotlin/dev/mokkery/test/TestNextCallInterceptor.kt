package dev.mokkery.test

import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.internal.interceptor.NextCallInterceptor

internal class TestNextCallInterceptor(
    interceptBlock: (MokkeryCallScope) -> Any? = { null },
    interceptSuspendBlock: suspend (MokkeryCallScope) -> Any? = { null }
) : TestMokkeryCallInterceptor(interceptBlock, interceptSuspendBlock), NextCallInterceptor
