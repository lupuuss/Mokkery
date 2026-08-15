package dev.mokkery.internal.dispatcher

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.context.MokkeryContext
import kotlin.reflect.KClass

internal fun MokkeryCallScope.availableSuperCallTypes(): List<KClass<*>> = superDispatcher
    ?.mokkeryCallSuperTypes(call.function.id)
    .orEmpty()

internal val MokkeryCallScope.superDispatcher: MokkerySuperCallDispatcher?
    get() = mokkeryContext[CallDispatchers]?.superDispatcher

internal val MokkeryCallScope.spyDispatcher: MokkerySpyCallDispatcher?
    get() = mokkeryContext[CallDispatchers]?.spyDispatcher


internal interface CallDispatchers : MokkeryContext.Element {

    override val key: Key get() = Key

    val spyDispatcher: MokkerySpyCallDispatcher?

    val superDispatcher: MokkerySuperCallDispatcher?

    companion object Key : MokkeryContext.Key<CallDispatchers>
}

internal fun callDispatchersContext(
    spyDispatcher: MokkerySpyCallDispatcher?,
    superDispatcher: MokkerySuperCallDispatcher?,
): MokkeryContext = when {
    spyDispatcher == null && superDispatcher == null -> MokkeryContext.Empty
    else -> CallDispatchersImpl(spyDispatcher, superDispatcher)
}

private class CallDispatchersImpl(
    override val spyDispatcher: MokkerySpyCallDispatcher?,
    override val superDispatcher: MokkerySuperCallDispatcher?,
) : CallDispatchers
