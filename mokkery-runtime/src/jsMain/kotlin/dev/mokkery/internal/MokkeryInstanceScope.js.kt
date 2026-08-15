package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkeryMockScope
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySpyScope
import dev.mokkery.configurer.MokkeryInstanceConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.dispatcher.MokkerySpyCallDispatcher
import kotlin.reflect.KClass

internal actual val Any.mokkeryScope: MokkeryInstanceScope?
    get() = this as? MokkeryInstanceScope ?: jsFunctionMokkeryScope

@Suppress("unused")
@PublishedApi
internal fun Any.setupMokkeryInstanceForJsFunction(
    parent: MokkeryScope,
    typeName: String,
    interceptedType: KClass<*>,
    typeArguments: List<KClass<*>> = emptyList(),
    mode: MockMode?,
    spiedObject: Any?,
    spyDispatcher: MokkerySpyCallDispatcher?,
    block: MokkeryInstanceConfigurer.Block<Any, *>?,
) {
    val scope = when {
        spiedObject != null -> MokkeryJsFunSpyScope(MokkeryContext.Empty)
        else -> MokkeryJsFunMockScope(MokkeryContext.Empty)
    }
    this.jsFunctionMokkeryScope = scope
    this.asDynamic().toString = scope::toString
    this.setupMokkeryInstance(
        parent = parent,
        typeName = typeName,
        interceptedTypes = listOf(interceptedType),
        typeArguments = listOf(typeArguments),
        mode = mode,
        spiedObject = spiedObject,
        defaultsExtractorFactory = null,
        spyDispatcher = spyDispatcher,
        superDispatcher = null,
        block = block,
    )
}

@PublishedApi
internal inline var Any.jsFunctionMokkeryScope: MokkeryInstanceScope?
    get() = this.asDynamic()._mokkeryScope as? MokkeryInstanceScope
    set(value) {
        this.asDynamic()._mokkeryScope = value
    }

private class MokkeryJsFunMockScope(
    override var mokkeryContext: MokkeryContext
) : MutableMokkeryMockScope {

    override fun toString(): String = instanceIdString
}

private class MokkeryJsFunSpyScope(
    override var mokkeryContext: MokkeryContext
) : MutableMokkerySpyScope {

    override fun toString(): String = instanceIdString
}
