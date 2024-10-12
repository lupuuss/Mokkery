@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import kotlin.reflect.KClass

internal interface MokkeryInstance {

    val id: String

    val interceptor: MokkeryInterceptor

    val interceptedTypes: List<KClass<*>>
}

internal interface MokkeryMockInstance : MokkeryInstance {

    override val interceptor: MokkeryMockInterceptor
}

internal fun MokkeryMockInstance(
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    mockedType: KClass<*>
): MokkeryMockInstance {
    return DynamicMokkeryMockInstance(mode, kind, typeName, listOf(mockedType))
}

private class DynamicMokkeryMockInstance(
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    override val interceptedTypes: List<KClass<*>>,
) : MokkeryMockInstance {
    override val interceptor = MokkeryMockInterceptor(mode, kind)

    override val id = MockUniqueReceiversGenerator.generate(typeName)
}
