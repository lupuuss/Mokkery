@file:Suppress("unused", "PropertyName")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.calls.CallTracingRegistry
import dev.mokkery.internal.calls.TemplatingSocket
import dev.mokkery.internal.context.CurrentMockContext
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.interceptor.MokkeryKind
import dev.mokkery.internal.interceptor.mokkeryMockInterceptor
import kotlin.reflect.KClass

internal interface MokkeryInstanceScope : MokkeryScope {

    val mokkeryInterceptor: MokkeryCallInterceptor
}

internal fun MokkeryScope.createMokkeryInstanceContext(
    typeName: String,
    mode: MockMode,
    kind: MokkeryKind,
    interceptedTypes: List<KClass<*>>,
    instance: MokkeryInstanceScope,
): MokkeryContext {
    return mokkeryContext
        .plus(CallTracingRegistry())
        .plus(AnsweringRegistry())
        .plus(TemplatingSocket())
        .plus(
            CurrentMockContext(
                id = tools.instanceIdGenerator.generate(typeName),
                mode = mode,
                kind = kind,
                interceptedTypes = interceptedTypes,
                self = instance
            )
        )
}

internal val MokkeryInstanceScope.mockId
    get() = mokkeryContext.require(CurrentMockContext).id

internal fun MokkeryInstanceScope(
    parent: MokkeryScope,
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    mockedType: KClass<*>
): MokkeryInstanceScope {
    return DynamicMokkeryInstanceScope(parent, mode, kind, typeName, listOf(mockedType))
}

private class DynamicMokkeryInstanceScope(
    parent: MokkeryScope,
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    interceptedTypes: List<KClass<*>>,
) : MokkeryInstanceScope {

    override val mokkeryInterceptor = mokkeryMockInterceptor()

    override val mokkeryContext = parent.createMokkeryInstanceContext(
        typeName = typeName,
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes,
        instance = this
    )
}
