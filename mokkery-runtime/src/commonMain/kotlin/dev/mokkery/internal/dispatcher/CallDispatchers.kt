package dev.mokkery.internal.dispatcher

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.context.MokkeryContext
import kotlin.reflect.KClass

internal fun MokkeryCallScope.availableSuperCallTypes(): List<KClass<*>> = superDispatcher
    ?.mokkeryCallSuperTypes(call.function.id)
    .orEmpty()

internal val MokkeryCallScope.superDispatcher: SuperCallDispatcher?
    get() = mokkeryContext[CallDispatchers]?.superDispatcher

internal val MokkeryCallScope.spyDispatcher: SpyCallDispatcher?
    get() = mokkeryContext[CallDispatchers]?.spyDispatcher


internal interface CallDispatchers : MokkeryContext.Element {

    override val key: Key get() = Key

    val spyDispatcher: SpyCallDispatcher?

    val superDispatcher: SuperCallDispatcher?

    companion object Key : MokkeryContext.Key<CallDispatchers>
}

internal fun callDispatchersContext(
    spyDispatcher: SpyCallDispatcher?,
    superDispatcher: SuperCallDispatcher?,
): MokkeryContext = when {
    spyDispatcher == null && superDispatcher == null -> MokkeryContext.Empty
    else -> CallDispatchersImpl(spyDispatcher, superDispatcher)
}

private class CallDispatchersImpl(
    override val spyDispatcher: SpyCallDispatcher?,
    override val superDispatcher: SuperCallDispatcher?,
) : CallDispatchers
