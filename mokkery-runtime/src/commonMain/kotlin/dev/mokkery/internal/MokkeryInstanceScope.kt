@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.memoized
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.context.MokkeryInstanceSpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.invokeInstantiationListener
import dev.mokkery.internal.context.requireSpy
import dev.mokkery.internal.context.settings
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.defaults.DefaultsExtractorFactory
import dev.mokkery.internal.interceptor.forkedHooksOrEmpty
import dev.mokkery.internal.interceptor.rootCallInterceptor
import dev.mokkery.internal.interceptor.rootInstantiationListener
import dev.mokkery.internal.rendering.instanceIdRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.tracing.CallTracingRegistry
import kotlin.reflect.KClass

internal fun MokkeryScope.instanceContext(
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<List<KClass<*>>>,
    thisRef: Any,
    mode: MockMode?,
    spiedObject: Any?,
    defaultsExtractorFactory: DefaultsExtractorFactory? = null
): MokkeryContext {
    val tools = tools
    val spec = MokkeryInstanceSpec.create(
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
    return mokkeryContext
        .plus(forkedHooksOrEmpty())
        .plus(rootInstantiationListener)
        .plus(spec)
        .plus(tools.callMatcherFactory.create(spec.collection))
        .plus(CallTracingRegistry())
        .plus(AnsweringRegistry())
        .plus(defaultsExtractorFactory ?: MokkeryContext.Empty)
        .memoized() // we memoize only context elements that probably won't change - ContextCallInterceptor will change
        .plus(rootCallInterceptor)
}

internal fun MokkeryInstanceScope.finalizeMokkeryInstance(
    thisRef: Any,
    block: (Any.() -> Unit)?,
) {
    block?.invoke(thisRef)
    invokeInstantiationListener(thisRef)
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
    get(): String = withRenderingScope(instances = this.instanceSpec.collection) {
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
