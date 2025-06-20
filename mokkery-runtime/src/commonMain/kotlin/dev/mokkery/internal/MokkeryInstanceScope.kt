@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.context.ContextCallInterceptor
import dev.mokkery.internal.context.ContextInstantiationListener
import dev.mokkery.internal.context.MokkeryInstanceSpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.memoized
import dev.mokkery.internal.context.requireSpy
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.defaults.DefaultsExtractorFactory
import dev.mokkery.internal.interceptor.AnsweringInterceptor
import dev.mokkery.internal.interceptor.CallTracingInterceptor
import dev.mokkery.internal.interceptor.MocksRegisteringListener
import dev.mokkery.internal.interceptor.MokkeryCallHooks
import dev.mokkery.internal.tracing.CallTracingRegistry
import kotlin.reflect.KClass

internal interface MokkeryInstanceScope : MokkeryScope

internal fun MokkeryInstanceScope(
    mokkeryContext: MokkeryContext
): MokkeryInstanceScope = object : MokkeryInstanceScope {
    override val mokkeryContext = mokkeryContext

    override fun toString(): String = instanceIdString
}

internal fun MokkeryScope.createInstanceScope(
    typeName: String,
    interceptedType: KClass<*>,
    typeArguments: List<KClass<*>> = emptyList(),
    thisRef: Any,
    mode: MockMode?,
    spiedObject: Any?
): MokkeryInstanceScope = MokkeryInstanceScope(
    createInstanceContext(
        mode = mode,
        typeName = typeName,
        interceptedTypes = listOf(interceptedType),
        typeArguments = listOf(typeArguments),
        thisRef = thisRef,
        spiedObject = spiedObject
    )
)


internal fun MokkeryScope.createInstanceContext(
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<List<KClass<*>>>,
    thisRef: Any,
    mode: MockMode?,
    spiedObject: Any?,
    defaultsExtractorFactory: DefaultsExtractorFactory? = null
): MokkeryContext = mokkeryContext
    .plus(ContextInstantiationListener(MocksRegisteringListener))
    .plus(
        MokkeryInstanceSpec.create(
            id = MokkeryInstanceId(typeName, tools.mocksCounter.next()),
            interceptedTypes = interceptedTypes,
            typeArguments = typeArguments,
            thisRef = thisRef,
            spiedObject = spiedObject,
            mode = mode,
        )
    )
    .plus(CallTracingRegistry())
    .plus(AnsweringRegistry())
    .plus(defaultsExtractorFactory ?: MokkeryContext.Empty)
    .memoized() // we memoize only context elements that probably won't change - ContextCallInterceptor will change
    .plus(
        ContextCallInterceptor(
            CallTracingInterceptor,
            MokkeryCallHooks.beforeAnswering,
            AnsweringInterceptor
        )
    )


internal expect val Any.mokkeryScope: MokkeryInstanceScope?

internal fun Any.requireInstanceScope(): MokkeryInstanceScope = mokkeryScope ?: throw ObjectNotMockedException(this)

internal val MokkeryInstanceScope.instanceId get() = instanceSpec.id

internal val MokkeryInstanceScope.instanceIdString get() = instanceSpec.id.toString()

internal val MokkeryInstanceScope.spiedObject get() = instanceSpec.requireSpy().spiedObject

internal fun MokkeryInstanceScope.typeArgumentAt(totalIndex: Int): KClass<*>? {
    var index = 0
    for (type in instanceSpec.interceptedTypes)
        for (typeArgument in type.arguments)
            if (totalIndex == index++) return typeArgument
    return null
}
