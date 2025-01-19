@file:Suppress("unused", "PropertyName")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.interceptor.MokkeryKind
import dev.mokkery.internal.interceptor.MokkeryMockInterceptor
import kotlin.reflect.KClass

internal interface MokkeryInstance {

    val _mokkeryId: String

    val _mokkeryInterceptor: MokkeryCallInterceptor

    val _mokkeryInterceptedTypes: List<KClass<*>>
}

@Suppress("NOTHING_TO_INLINE")
internal inline val MokkeryInstance.id get() = _mokkeryId

@Suppress("NOTHING_TO_INLINE")
internal inline val MokkeryInstance.interceptor get() = _mokkeryInterceptor

@Suppress("NOTHING_TO_INLINE")
internal inline val MokkeryInstance.interceptedTypes get() = _mokkeryInterceptedTypes

internal interface MokkeryMockInstance : MokkeryInstance {

    override val _mokkeryInterceptor: MokkeryMockInterceptor
}

@Suppress("NOTHING_TO_INLINE")
internal inline val MokkeryMockInstance.interceptor get() = _mokkeryInterceptor

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
    override val _mokkeryInterceptedTypes: List<KClass<*>>,
) : MokkeryMockInstance {
    override val _mokkeryInterceptor = MokkeryMockInterceptor(mode, kind)

    override val _mokkeryId = GlobalMokkeryScope.tools.instanceIdGenerator.generate(typeName)
}
