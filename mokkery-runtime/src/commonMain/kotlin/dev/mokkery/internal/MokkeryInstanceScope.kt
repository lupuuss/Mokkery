@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkeryScope
import dev.mokkery.configurer.MokkeryInstanceConfigurer
import dev.mokkery.configurer.MokkeryMockConfigurer
import dev.mokkery.configurer.MokkerySpyConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.keepOnTop
import dev.mokkery.context.memoized
import dev.mokkery.context.require
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.configurer.BaseMokkeryConfigurer
import dev.mokkery.internal.context.MokkeryInstanceSpec
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.invokeInstantiationListener
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

@PublishedApi
internal fun Any.setupMokkeryInstance(
    parent: MokkeryScope,
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<List<KClass<*>>>,
    mode: MockMode?,
    spiedObject: Any?,
    defaultsExtractorFactory: DefaultsExtractorFactory?,
    setContext: (MokkeryContext) -> Unit,
    block: MokkeryInstanceConfigurer.Block<Any, *>?,
) {
    val baseContext = parent.instanceContext(
        typeName = typeName,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        thisRef = this,
        mode = mode,
        spiedObject = spiedObject,
        defaultsExtractorFactory = defaultsExtractorFactory
    )
    setContext(baseContext)
    // now instance is in a "preconfigured" state
    // we can apply user provided block with additional configuration
    if (block != null) {
        this.applyConfigurerBlock(baseContext, setContext, block)
    }
    this.invokeInstantiationListener()
}

private fun Any.applyConfigurerBlock(
    context: MokkeryContext,
    setContext: (MokkeryContext) -> Unit,
    block: MokkeryInstanceConfigurer.Block<Any, *>
) {
    val configurer = when (context.require(MokkeryInstanceSpec)) {
        is MokkeryMockSpec -> MokkeryMockConfigurerImpl(context, setContext)
        is MokkerySpySpec -> MokkerySpyConfigurerImpl(context, setContext)
    }
    val aware = MokkeryInstanceConfigurerAwareImpl(this, configurer)
    aware.use { block(it, this) }
}

private fun MokkeryScope.instanceContext(
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<List<KClass<*>>>,
    thisRef: Any,
    mode: MockMode?,
    spiedObject: Any?,
    defaultsExtractorFactory: DefaultsExtractorFactory?
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
        .keepOnTop(rootCallInterceptor)
}

internal expect val Any.mokkeryScope: MokkeryInstanceScope?

internal val Any?.isMock: Boolean
    get() = this?.mokkeryScope != null

internal val Any?.isNotMock: Boolean
    get() = !isMock

internal fun Any.requireInstanceScope(): MokkeryInstanceScope = mokkeryScope ?: throw ObjectNotMockedException(this)

internal val MokkeryInstanceScope.instanceId get() = instanceSpec.id

@PublishedApi
internal val MokkeryInstanceScope.instanceIdString: String
    get() = withRenderingScope {
        instanceIdRenderer.render(instanceId)
    }

internal val MokkeryInstanceScope.shortInstanceIdString
    get(): String = withRenderingScope(instances = this.instanceSpec.collection) {
        instanceIdRenderer.render(instanceId)
    }

@PublishedApi
internal fun MokkeryInstanceScope.typeArgumentAt(totalIndex: Int): KClass<*>? {
    var index = 0
    for (type in instanceSpec.interceptedTypes)
        for (typeArgument in type.arguments)
            if (totalIndex == index++) return typeArgument
    return null
}

private class MokkerySpyConfigurerImpl(
    context: MokkeryContext,
    setContext: (MokkeryContext) -> Unit
) : MokkeryInstanceConfigurerImpl(context, setContext), MokkerySpyConfigurer

private class MokkeryMockConfigurerImpl(
    context: MokkeryContext,
    setContext: (MokkeryContext) -> Unit
) : MokkeryInstanceConfigurerImpl(context, setContext), MokkeryMockConfigurer

private abstract class MokkeryInstanceConfigurerImpl(
    context: MokkeryContext,
    private val setContext: (MokkeryContext) -> Unit,
) : BaseMokkeryConfigurer(context), MokkeryInstanceConfigurer {

    override var mokkeryContext: MokkeryContext
        get() = super.mokkeryContext
        set(value) {
            super.mokkeryContext = value
            setContext(value)
        }
}

private class MokkeryInstanceConfigurerAwareImpl<T, C>(
    private val ref: T,
    private val configurer: C,
) : MokkeryInstanceConfigurer.Aware<T, C>, AutoCloseable
        where T : Any, C : MokkeryInstanceConfigurer, C : AutoCloseable {

    override fun configurer(value: T): C {
        if (ref !== value) mokkeryRuntimeError("This configuration block only allows configuring $ref, but tried to configure $value")
        return configurer
    }

    override fun close() {
        configurer.close()
    }
}
