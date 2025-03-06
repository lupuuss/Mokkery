@file:Suppress("unused", "PropertyName")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.internal.context.CurrentMockContext
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.interceptor.MokkeryKind
import dev.mokkery.internal.interceptor.MokkeryMockInterceptor
import kotlin.reflect.KClass

internal interface MokkeryInstance : MokkeryScope {

    val mokkeryInterceptor: MokkeryCallInterceptor
}

internal fun MokkeryScope.createMokkeryInstanceContext(
    typeName: String,
    mode: MockMode,
    kind: MokkeryKind,
    interceptedTypes: List<KClass<*>>,
    instance: MokkeryInstance,
): MokkeryContext {
    return mokkeryContext + CurrentMockContext(
        id = tools.instanceIdGenerator.generate(typeName),
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes,
        self = instance
    )
}

internal val MokkeryInstance.mockId
    get() = mokkeryContext.require(CurrentMockContext).id

internal interface MokkeryMockInstance : MokkeryInstance {

    override val mokkeryInterceptor: MokkeryMockInterceptor
}

internal fun MokkeryMockInstance(
    parent: MokkeryScope,
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    mockedType: KClass<*>
): MokkeryMockInstance {
    return DynamicMokkeryMockInstance(parent, mode, kind, typeName, listOf(mockedType))
}

private class DynamicMokkeryMockInstance(
    parent: MokkeryScope,
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    interceptedTypes: List<KClass<*>>,
) : MokkeryMockInstance {

    override val mokkeryInterceptor = MokkeryMockInterceptor()

    override val mokkeryContext = parent.createMokkeryInstanceContext(
        typeName = typeName,
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes,
        instance = this
    )
}
