package dev.mokkery.internal.options

import dev.mokkery.annotations.InternalMokkeryApi

@InternalMokkeryApi
public data class MokkeryOption<T>(
    public val name: String,
    public val description: String,
    public val required: Boolean,
    public val allowMultipleOccurrences: Boolean,
    public val type: MokkeryOptionType<T>,
    public val defaultValue: T?,
    public val extras: MutableMap<Any?, Any?> = mutableMapOf(),
)

@InternalMokkeryApi
public fun interface MokkeryOptionProjection<out P> {

    public fun create(option: MokkeryOption<*>): P
}

@Suppress("UNCHECKED_CAST")
@InternalMokkeryApi
public fun <P> MokkeryOption<*>.get(projection: MokkeryOptionProjection<P>): P {
    return extras.getOrPut(projection) { projection.create(this) } as P
}
