@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.calls.CallTracingRegistry
import dev.mokkery.internal.calls.TemplatingSocket
import dev.mokkery.internal.context.MockContext
import dev.mokkery.internal.context.mockContext
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.interceptor.AnsweringInterceptor
import dev.mokkery.internal.interceptor.CallTracingInterceptor
import dev.mokkery.internal.interceptor.MockInterceptor
import dev.mokkery.internal.interceptor.MokkeryCallHooks
import dev.mokkery.internal.interceptor.MokkeryKind
import dev.mokkery.internal.interceptor.TemplatingInterceptor
import kotlin.reflect.KClass

internal interface MokkeryInstanceScope : MokkeryScope

internal fun MokkeryScope.createMokkeryInstanceContext(
    typeName: String,
    mode: MockMode,
    kind: MokkeryKind,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<KClass<*>>,
    scope: MokkeryInstanceScope,
    spiedObject: Any?
): MokkeryContext {
    return mokkeryContext
        .plus(
            MockContext(
                id = tools.instanceIdGenerator.generate(typeName),
                mode = mode,
                kind = kind,
                interceptedTypes = interceptedTypes,
                typeArguments = typeArguments,
                spiedObject = spiedObject,
                thisInstanceScope = scope
            )
        )
        .plus(CallTracingRegistry())
        .plus(AnsweringRegistry())
        .plus(TemplatingSocket())
        .plus(
            MockInterceptor(
                TemplatingInterceptor,
                CallTracingInterceptor,
                MokkeryCallHooks.beforeAnswering,
                AnsweringInterceptor
            )
        )
}

internal val MokkeryInstanceScope.mockId get() = mockContext.id

internal val MokkeryInstanceScope.spiedObject get() = mockContext.spiedObject

internal fun MokkeryInstanceScope.typeArgumentAt(index: Int): KClass<*>? = mockContext.typeArguments.getOrNull(index)

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

    override val mokkeryContext = parent.createMokkeryInstanceContext(
        typeName = typeName,
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        scope = this,
        spiedObject = spiedObject
    )
}
