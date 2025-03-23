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
import dev.mokkery.internal.context.currentMockContext
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
    typeArguments: List<KClass<*>>,
    instance: MokkeryInstanceScope,
    spiedObject: Any?
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
                typeArguments = typeArguments,
                self = instance,
                spiedObject = spiedObject
            )
        )
}

internal val MokkeryInstanceScope.mockId
    get() = mokkeryContext.require(CurrentMockContext).id

internal val MokkeryInstanceScope.spiedObject
    get() = mokkeryContext.require(CurrentMockContext).spiedObject

internal fun MokkeryInstanceScope.typeArgumentAt(index: Int): KClass<*>? = currentMockContext
    .typeArguments
    .getOrNull(index)

internal fun MokkeryInstanceScope(
    parent: MokkeryScope,
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    mockedType: KClass<*>,
    typeArguments: List<KClass<*>> = emptyList(),
    spiedObject: Any?
): MokkeryInstanceScope = DynamicMokkeryInstanceScope(
    parent = parent,
    mode = mode,
    kind = kind,
    typeName = typeName,
    interceptedTypes = listOf(mockedType),
    typeArguments = typeArguments,
    spiedObject = spiedObject
)

private class DynamicMokkeryInstanceScope(
    parent: MokkeryScope,
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<KClass<*>> = emptyList(),
    spiedObject: Any?,
) : MokkeryInstanceScope {

    override val mokkeryInterceptor = mokkeryMockInterceptor()

    override val mokkeryContext = parent.createMokkeryInstanceContext(
        typeName = typeName,
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        instance = this,
        spiedObject = spiedObject
    )
}
