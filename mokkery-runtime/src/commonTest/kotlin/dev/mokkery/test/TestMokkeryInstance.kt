package dev.mokkery.test

import dev.mokkery.internal.MokkeryInstance
import kotlin.reflect.KClass

internal data class TestMokkeryInstance(
    override val id: String = "Test",
    override val interceptor: TestMokkeryInterceptor = TestMokkeryInterceptor(),
    override val interceptedTypes: List<KClass<*>> = listOf(Unit::class)
) : MokkeryInstance
