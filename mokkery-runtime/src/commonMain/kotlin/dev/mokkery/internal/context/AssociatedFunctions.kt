package dev.mokkery.internal.context

import dev.mokkery.MokkeryCallScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.utils.bestName
import kotlin.reflect.KClass

internal val MokkeryCallScope.associatedFunctions: AssociatedFunctions
    get() = mokkeryContext.require(AssociatedFunctions)

internal interface AssociatedFunctions: MokkeryContext.Element {
    val supers: Map<KClass<*>, Function<Any?>>
    val spiedFunction: Function<Any?>?

    override val key get() = Key

    companion object Key : MokkeryContext.Key<AssociatedFunctions>
}

internal fun AssociatedFunctions(
    supers: Map<KClass<*>, Function<Any?>>,
    spiedFunction: Function<Any?>?
): AssociatedFunctions = when {
    supers.isEmpty() && spiedFunction == null -> EmptyAssociatedFunctions
    else -> AssociatedFunctionsImpl(supers, spiedFunction)
}

private class AssociatedFunctionsImpl(
    override val supers: Map<KClass<*>, Function<Any?>>,
    override val spiedFunction: Function<Any?>?
) : AssociatedFunctions {

    override fun toString(): String = buildString {
        val supersString = supers
            .takeIf { it.isNotEmpty() }
            ?.keys
            ?.joinToString { "${it.bestName()}={...}" }
            ?.let { "supers=$it" }
        val spiedFunctionString = "spiedFunction={...}".takeIf { spiedFunction != null }
        append("AssociatedFunctions(")
        append(listOfNotNull(supersString, spiedFunctionString).joinToString())
        append(")")
    }
}

private object EmptyAssociatedFunctions : AssociatedFunctions {

    override val supers: Map<KClass<*>, Function<Any?>> = emptyMap()
    override val spiedFunction: Function<Any?>? = null

    override fun toString(): String = "AssociatedFunctions()"
}
