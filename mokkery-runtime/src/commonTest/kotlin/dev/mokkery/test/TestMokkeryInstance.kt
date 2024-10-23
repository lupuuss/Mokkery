package dev.mokkery.test

import dev.mokkery.internal.MokkeryInstance
import kotlin.reflect.KClass

internal data class TestMokkeryInstance(
    override val _mokkeryId: String = "Test",
    override val _mokkeryInterceptor: TestMokkeryInterceptor = TestMokkeryInterceptor(),
    override val _mokkeryInterceptedTypes: List<KClass<*>> = listOf(Unit::class)
) : MokkeryInstance
