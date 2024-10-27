package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import kotlin.reflect.KClass

internal val MokkeryContext.associatedFunctions
    get() = get(AssociatedFunctions) ?: error("Associated functions not found!")

internal class AssociatedFunctions(
    val supers: Map<KClass<*>, Function<Any?>>,
    val spiedFunction: Function<Any?>?
) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<AssociatedFunctions>
}
