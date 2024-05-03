@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import kotlin.reflect.KClass

internal interface MokkeryInterceptorScope {

    val id: String

    val interceptor: MokkeryInterceptor

    val interceptedTypes: List<KClass<*>>
}

internal interface MokkerySpyScope : MokkeryInterceptorScope {

    override val interceptor: MokkerySpy
}


internal interface MokkeryMockScope : MokkerySpyScope {

    override val interceptor: MokkeryMock
}


internal fun MokkerySpyScope(typeName: String, mockedTypes: List<KClass<*>>): MokkerySpyScope {
    return DynamicMokkerySpyScope(typeName, mockedTypes)
}

internal fun MokkeryMockScope(mode: MockMode, typeName: String, mockedTypes: List<KClass<*>>): MokkeryMockScope {
    return DynamicMokkeryMockScope(mode, typeName, mockedTypes)
}

private class DynamicMokkeryMockScope(
    mode: MockMode,
    typeName: String,
    override val interceptedTypes: List<KClass<*>>,
) : MokkeryMockScope {
    override val interceptor = MokkeryMock(mode)

    override val id = generateMockId(typeName)
}

private class DynamicMokkerySpyScope(
    typeName: String,
    override val interceptedTypes: List<KClass<*>>
) : MokkerySpyScope {

    override val interceptor = MokkerySpy()

    override val id = generateMockId(typeName)
}
