@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.calls.CallTracingRegistry
import dev.mokkery.internal.calls.TemplatingSocket
import dev.mokkery.internal.context.ContextCallInterceptor
import dev.mokkery.internal.context.ContextInstantiationListener
import dev.mokkery.internal.context.MockSpec
import dev.mokkery.internal.context.memoized
import dev.mokkery.internal.context.mockSpec
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.interceptor.AnsweringInterceptor
import dev.mokkery.internal.interceptor.CallTracingInterceptor
import dev.mokkery.internal.interceptor.MocksRegisteringListener
import dev.mokkery.internal.interceptor.MokkeryCallHooks
import dev.mokkery.internal.interceptor.TemplatingInterceptor
import kotlin.reflect.KClass

internal interface MokkeryInstanceScope : MokkeryScope

internal fun MokkeryScope.createMokkeryInstanceContext(
    typeName: String,
    mode: MockMode,
    kind: MokkeryKind,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<KClass<*>>,
    thisRef: Any,
    spiedObject: Any?
): MokkeryContext = mokkeryContext
    .plus(ContextInstantiationListener(MocksRegisteringListener))
    .plus(
        MockSpec(
            id = MockId(typeName, tools.mocksCounter.next()),
            mode = mode,
            kind = kind,
            interceptedTypes = interceptedTypes,
            typeArguments = typeArguments,
            spiedObject = spiedObject,
            thisRef = thisRef
        )
    )
    .plus(CallTracingRegistry())
    .plus(AnsweringRegistry())
    .plus(TemplatingSocket())
    .memoized() // we memoize only context elements that probably won't change - ContextCallInterceptor will change
    .plus(
        ContextCallInterceptor(
            TemplatingInterceptor,
            CallTracingInterceptor,
            MokkeryCallHooks.beforeAnswering,
            AnsweringInterceptor
        )
    )

internal val MokkeryInstanceScope.mockId get() = mockSpec.id

internal val MokkeryInstanceScope.mockIdString get() = mockSpec.id.toString()

internal val MokkeryInstanceScope.spiedObject get() = mockSpec.spiedObject

internal fun MokkeryInstanceScope.typeArgumentAt(index: Int): KClass<*>? = mockSpec.typeArguments.getOrNull(index)

internal fun Any.requireInstanceScope(): MokkeryInstanceScope = mokkeryScope ?: throw ObjectNotMockedException(this)

internal expect val Any.mokkeryScope: MokkeryInstanceScope?

internal fun MokkeryInstanceScope(
    parent: MokkeryScope,
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    mockedType: KClass<*>,
    typeArguments: List<KClass<*>> = emptyList(),
    thisRef: Any,
    spiedObject: Any?
): MokkeryInstanceScope = DynamicMokkeryInstanceScope(
    parent = parent,
    mode = mode,
    kind = kind,
    typeName = typeName,
    interceptedTypes = listOf(mockedType),
    typeArguments = typeArguments,
    thisRef = thisRef,
    spiedObject = spiedObject
)

private class DynamicMokkeryInstanceScope(
    parent: MokkeryScope,
    mode: MockMode,
    kind: MokkeryKind,
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<KClass<*>> = emptyList(),
    thisRef: Any,
    spiedObject: Any?,
) : MokkeryInstanceScope {

    override val mokkeryContext = parent.createMokkeryInstanceContext(
        typeName = typeName,
        mode = mode,
        kind = kind,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        thisRef = thisRef,
        spiedObject = spiedObject
    )

    override fun toString(): String = mockIdString
}
