@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import kotlin.reflect.KClass

internal interface MokkeryInterceptorScope {

    val id: String

    val interceptor: MokkeryInterceptor

    val interceptedTypes: List<KClass<*>>
}

internal interface MokkeryMockScope : MokkeryInterceptorScope {

    override val interceptor: MokkeryMock
}

internal fun MokkeryMockScope(
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    mockedType: KClass<*>
): MokkeryMockScope {
    return DynamicMokkeryMockScope(mode, kind, typeName, listOf(mockedType))
}

private class DynamicMokkeryMockScope(
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    override val interceptedTypes: List<KClass<*>>,
) : MokkeryMockScope {
    override val interceptor = MokkeryMock(mode, kind)

    override val id = MockUniqueReceiversGenerator.generate(typeName)
}
