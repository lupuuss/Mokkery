package dev.mokkery.test

import dev.mokkery.internal.MokkeryInterceptorScope
import kotlin.reflect.KClass

internal data class TestMokkeryInterceptorScope(
    override val id: String = "Test",
    override val interceptor: TestMokkeryInterceptor = TestMokkeryInterceptor(),
    override val interceptedTypes: List<KClass<*>> = listOf(Unit::class)
) : MokkeryInterceptorScope
