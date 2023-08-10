package dev.mokkery.test

import dev.mokkery.internal.MokkeryInterceptorScope
import kotlin.reflect.KClass

internal data class TestMokkeryInterceptorScope(
    override val id: String = "Test",
    override val interceptor: TestMokkeryInterceptor = TestMokkeryInterceptor(),
    override val interceptedType: KClass<*> = Unit::class
) : MokkeryInterceptorScope
