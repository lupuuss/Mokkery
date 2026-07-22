@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkeryMockScope
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySpyScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.memoized
import dev.mokkery.context.require
import dev.mokkery.interceptor.callHooks
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.context.ContextCallInterceptor
import dev.mokkery.internal.context.ContextInstantiationListener
import dev.mokkery.internal.context.MokkeryInstanceSpec
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.requireSpy
import dev.mokkery.internal.context.settings
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.defaults.DefaultsExtractorFactory
import dev.mokkery.internal.interceptor.AnsweringInterceptor
import dev.mokkery.internal.interceptor.CallTracingInterceptor
import dev.mokkery.internal.interceptor.MocksRegisteringListener
import dev.mokkery.internal.interceptor.fork
import dev.mokkery.internal.rendering.instanceIdRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.tracing.CallTracingRegistry
import kotlin.reflect.KClass

internal fun MokkeryMockScope(
    mokkeryContext: MokkeryContext
): MokkeryMockScope = object : MokkeryMockScope {
    override val mokkeryContext = mokkeryContext

    override fun toString(): String = instanceIdString
}

internal fun MokkerySpyScope(
    mokkeryContext: MokkeryContext
): MokkerySpyScope = object : MokkerySpyScope {
    override val mokkeryContext = mokkeryContext

    override fun toString(): String = instanceIdString
}

internal fun MokkeryScope.instanceScope(
    typeName: String,
    interceptedType: KClass<*>,
    typeArguments: List<KClass<*>> = emptyList(),
    thisRef: Any,
    mode: MockMode?,
    spiedObject: Any?
): MokkeryInstanceScope {
    val context = instanceContext(
        mode = mode,
        typeName = typeName,
        interceptedTypes = listOf(interceptedType),
        typeArguments = listOf(typeArguments),
        thisRef = thisRef,
        spiedObject = spiedObject
    )
    return when (context.require(MokkeryInstanceSpec)) {
        is MokkeryMockSpec -> MokkeryMockScope(context)
        is MokkerySpySpec -> MokkerySpyScope(context)
    }
}


internal fun MokkeryScope.instanceContext(
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<List<KClass<*>>>,
    thisRef: Any,
    mode: MockMode?,
    spiedObject: Any?,
    defaultsExtractorFactory: DefaultsExtractorFactory? = null
): MokkeryContext {
    val hooks = callHooks.fork()
    return mokkeryContext
        .plus(hooks)
        .plus(ContextInstantiationListener(MocksRegisteringListener))
        .plus(
            MokkeryInstanceSpec.create(
                id = MokkeryInstanceId(typeName, tools.mocksCounter.next()),
                interceptedTypes = interceptedTypes,
                typeArguments = typeArguments,
                thisRef = thisRef,
                spiedObject = spiedObject,
                mode = when {
                    spiedObject != null -> null
                    else -> mode ?: settings.defaultMockMode
                },
            )
        )
        .plus(CallTracingRegistry())
        .plus(AnsweringRegistry())
        .plus(defaultsExtractorFactory ?: MokkeryContext.Empty)
        .memoized() // we memoize only context elements that probably won't change - ContextCallInterceptor will change
        .plus(
            ContextCallInterceptor(
                hooks.beforeTracing,
                CallTracingInterceptor,
                hooks.beforeAnswering,
                AnsweringInterceptor
            )
        )
}


internal expect val Any.mokkeryScope: MokkeryInstanceScope?

internal val Any?.isMock: Boolean
    get() = this?.mokkeryScope != null

internal val Any?.isNotMock: Boolean
    get() = !isMock

internal fun Any.requireInstanceScope(): MokkeryInstanceScope = mokkeryScope ?: throw ObjectNotMockedException(this)

internal val MokkeryInstanceScope.instanceId get() = instanceSpec.id

internal val MokkeryInstanceScope.instanceIdString
    get() = withRenderingScope {
        instanceIdRenderer.render(instanceId)
    }

internal val MokkeryInstanceScope.shortInstanceIdString
    get(): String = withRenderingScope(instances = this.toMokkeryCollection()) {
        instanceIdRenderer.render(instanceId)
    }
internal val MokkeryInstanceScope.spiedObject get() = instanceSpec.requireSpy().spiedObject

internal fun MokkeryInstanceScope.typeArgumentAt(totalIndex: Int): KClass<*>? {
    var index = 0
    for (type in instanceSpec.interceptedTypes)
        for (typeArgument in type.arguments)
            if (totalIndex == index++) return typeArgument
    return null
}
