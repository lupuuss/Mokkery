package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.MokkeryCallScope
import dev.mokkery.internal.utils.bestName
import kotlin.reflect.KClass

internal val MokkeryCallScope.associatedFunctions: AssociatedFunctions
    get() = mokkeryContext.require(AssociatedFunctions)

internal class AssociatedFunctions(
    val supers: Map<KClass<*>, Function<Any?>>,
    val spiedFunction: Function<Any?>?
) : MokkeryContext.Element {

    override fun toString(): String = "AssociatedFunctions(" +
            "supers={${supers.entries.joinToString { it.key.bestName() + "={...}" }}}, " +
            "spiedFunction=${"{...}".takeIf { spiedFunction != null }})"


    override val key = Key

    companion object Key : MokkeryContext.Key<AssociatedFunctions>
}
