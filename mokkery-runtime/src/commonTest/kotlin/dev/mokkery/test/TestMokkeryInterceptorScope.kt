package dev.mokkery.test

import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.MokkeryInterceptorScope

internal data class TestMokkeryInterceptorScope(
    override val id: String = "Test",
    override val interceptor: TestMokkeryInterceptor = TestMokkeryInterceptor()
) : MokkeryInterceptorScope
