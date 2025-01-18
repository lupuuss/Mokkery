package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryCallScope
import kotlin.reflect.KClass

internal val MokkeryCallScope.associatedFunctions: AssociatedFunctions
    get() = context.require(AssociatedFunctions)

internal class AssociatedFunctions(
    val supers: Map<KClass<*>, Function<Any?>>,
    val spiedFunction: Function<Any?>?
) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<AssociatedFunctions>
}
