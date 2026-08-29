@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkeryMockScope
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySpyScope
import dev.mokkery.configurer.MokkeryInstanceConfigurer
import dev.mokkery.configurer.MokkeryMockConfigurer
import dev.mokkery.configurer.MokkerySpyConfigurer
import dev.mokkery.context.Function
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.keepOnTop
import dev.mokkery.context.require
import dev.mokkery.context.withMemoized
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.configurer.ClosableMokkeryConfigurer
import dev.mokkery.internal.context.MemberFunctions
import dev.mokkery.internal.context.MokkeryInstanceSpec
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.invokeInstantiationListener
import dev.mokkery.internal.context.settings
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.contracts.InstanceContractsProvider
import dev.mokkery.internal.contracts.memberFunctions
import dev.mokkery.internal.defaults.DefaultsExtractingInterceptor
import dev.mokkery.internal.interceptor.forkedHooksOrEmpty
import dev.mokkery.internal.interceptor.rootCallInterceptor
import dev.mokkery.internal.interceptor.rootInstantiationListener
import dev.mokkery.internal.rendering.instanceIdRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.tracing.CallTracingRegistry
import kotlin.reflect.KClass

@PublishedApi
internal interface MutableMokkeryInstanceScope : MokkeryInstanceScope {

    override var mokkeryContext: MokkeryContext
}

@PublishedApi
internal interface MutableMokkeryMockScope : MutableMokkeryInstanceScope, MokkeryMockScope

@PublishedApi
internal interface MutableMokkerySpyScope : MutableMokkeryInstanceScope, MokkerySpyScope

// not needed for JS function
@PublishedApi
internal fun Any.setupMokkeryInstanceForDefaults(
    owner: Any,
    functionId: Long,
) {
    val scope = this.requireInstanceScope() as MutableMokkeryInstanceScope
    val extractorSpec = owner.requireInstanceScope()
        .instanceSpec
        .defaultsExtractorSpec(this)
    val contracts = InstanceContractsProvider(this)
    scope.mokkeryContext = extractorSpec
        .plus(contracts)
        .plus(DefaultsExtractingInterceptor(Function.Id(functionId)))
        .plus(MemberFunctions.cached(contracts.memberFunctions))
}

@PublishedApi
internal fun Any.setupMokkeryInstanceForCommon(
    parent: MokkeryScope,
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<List<KClass<*>>>,
    mode: MockMode?,
    spiedObject: Any?,
    block: MokkeryInstanceConfigurer.Block<Any, *>?,
): Unit = setupMokkeryInstance(
    parent = parent,
    typeName = typeName,
    interceptedTypes = interceptedTypes,
    typeArguments = typeArguments,
    mode = mode,
    spiedObject = spiedObject,
    contractsProvider = InstanceContractsProvider(this),
    block = block,
)

internal fun Any.setupMokkeryInstance(
    parent: MokkeryScope,
    typeName: String,
    interceptedTypes: List<KClass<*>>,
    typeArguments: List<List<KClass<*>>>,
    mode: MockMode?,
    spiedObject: Any?,
    contractsProvider: InstanceContractsProvider,
    block: MokkeryInstanceConfigurer.Block<Any, *>?,
) {
    val baseContext = parent.instanceContext(
        typeName = typeName,
        interceptedTypes = interceptedTypes,
        typeArguments = typeArguments,
        thisRef = this,
        mode = mode,
        spiedObject = spiedObject,
        contracts = contractsProvider,
    )
    val scope = this.mokkeryScope as MutableMokkeryInstanceScope
    scope.mokkeryContext = baseContext
    // now instance is in a "preconfigured" state
    // we can apply user provided block with additional configuration
    if (block != null) {
        this.applyConfigurerBlock(scope, block)
    }
    this.invokeInstantiationListener()
}

private fun MokkeryInstanceSpec.defaultsExtractorSpec(ref: Any) = when (this) {
    is MokkeryMockSpec -> MokkeryMockSpec(
        id = id.defaultsExtractorId(),
        thisRef = ref,
        interceptedTypes = interceptedTypes,
        mode = mode,
    )
    is MokkerySpySpec -> MokkerySpySpec(
        id = id.defaultsExtractorId(),
        thisRef = ref,
        interceptedTypes = interceptedTypes,
        spiedObject = spiedObject,
    )
}

private fun MokkeryInstanceId.defaultsExtractorId(): MokkeryInstanceId = MokkeryInstanceId($$"$${typeName}$DefaultsExtractor", id)

private fun Any.applyConfigurerBlock(
    scope: MutableMokkeryInstanceScope,
    block: MokkeryInstanceConfigurer.Block<Any, *>
) {
    val configurer = when (scope.mokkeryContext.require(MokkeryInstanceSpec)) {
        is MokkeryMockSpec -> MokkeryMockConfigurerImpl(scope)
        is MokkerySpySpec -> MokkerySpyConfigurerImpl(scope)
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
    contracts: InstanceContractsProvider,
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
        .withMemoized {
            +forkedHooksOrEmpty()
            +rootInstantiationListener
            +spec
            +tools.callMatcherFactory.create(spec.collection)
            +CallTracingRegistry()
            +AnsweringRegistry()
            +contracts
            +MemberFunctions.cached(contracts.memberFunctions)
        }.keepOnTop(rootCallInterceptor)
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
    get() = withRenderingScope(useShortening = false) { instanceIdRenderer.render(instanceId) }

internal val MokkeryInstanceScope.shortInstanceIdString
    get(): String = withRenderingScope { instanceIdRenderer.render(instanceId) }

@PublishedApi
internal fun MokkeryInstanceScope.typeArgumentAt(totalIndex: Int): KClass<*> {
    val spec = instanceSpec
    var index = 0
    for (type in spec.interceptedTypes)
        for (typeArgument in type.arguments)
            if (totalIndex == index++) return typeArgument
    return Any::class
}

private class MokkerySpyConfigurerImpl(
    scope: MutableMokkeryInstanceScope
) : MokkeryInstanceConfigurerImpl(scope), MokkerySpyConfigurer

private class MokkeryMockConfigurerImpl(
    scope: MutableMokkeryInstanceScope
) : MokkeryInstanceConfigurerImpl(scope), MokkeryMockConfigurer

private abstract class MokkeryInstanceConfigurerImpl(
    private val scope: MutableMokkeryInstanceScope,
) : ClosableMokkeryConfigurer(), MokkeryInstanceConfigurer {

    override var mokkeryContext: MokkeryContext
        get() = ensureOpen { scope.mokkeryContext }
        set(value) = ensureOpen { scope.mokkeryContext = value }
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
