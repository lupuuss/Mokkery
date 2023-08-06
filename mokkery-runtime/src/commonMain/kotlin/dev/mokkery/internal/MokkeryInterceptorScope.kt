@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import kotlin.reflect.KClass

internal interface MokkeryInterceptorScope {

    val id: String

    val interceptor: MokkeryInterceptor

    val interceptedType: KClass<*>
}

internal interface MokkerySpyScope : MokkeryInterceptorScope {

    override val interceptor: MokkerySpy
}


internal interface MokkeryMockScope : MokkerySpyScope {

    override val interceptor: MokkeryMock
}


internal fun MokkerySpyScope(typeName: String, mockedType: KClass<*>): MokkerySpyScope {
    return DynamicMokkerySpyScope(typeName, mockedType)
}

internal fun MokkeryMockScope(mode: MockMode, typeName: String, mockedType: KClass<*>): MokkeryMockScope {
    return DynamicMokkeryMockScope(mode, typeName, mockedType)
}

private class DynamicMokkeryMockScope(
    mode: MockMode,
    typeName: String,
    override val interceptedType: KClass<*>,
) : MokkeryMockScope {
    override val interceptor = MokkeryMock(mode)

    override val id = generateMockId(typeName)
}

private class DynamicMokkerySpyScope(typeName: String, override val interceptedType: KClass<*>) : MokkerySpyScope {

    override val interceptor = MokkerySpy()

    override val id = generateMockId(typeName)
}
