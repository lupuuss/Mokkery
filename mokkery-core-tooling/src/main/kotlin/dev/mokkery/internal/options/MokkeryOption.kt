package dev.mokkery.internal.options

import dev.mokkery.annotations.InternalMokkeryApi

@InternalMokkeryApi
public data class MokkeryOption<T>(
    public val name: String,
    public val description: String,
    public val required: Boolean,
    public val allowMultipleOccurrences: Boolean,
    public val type: MokkeryOptionType<T>,
    public val defaultValues: List<T>,
)

@InternalMokkeryApi
public interface MokkeryOptionProjection<out P> {

    public fun project(option: MokkeryOption<*>): P

    @InternalMokkeryApi
    public companion object {

        public operator fun <T> invoke(
            builder: (MokkeryOption<*>) -> T
        ): MokkeryOptionProjection<T> = DefaultMokkeryOptionProjection(builder)

        public fun <T> cached(
            builder: (MokkeryOption<*>) -> T
        ): MokkeryOptionProjection<T> = CachedMokkeryProjection(builder)
    }
}

private class DefaultMokkeryOptionProjection<T>(
    private val builder: (MokkeryOption<*>) -> T
) : MokkeryOptionProjection<T> {
    override fun project(option: MokkeryOption<*>): T = builder(option)
}

private class CachedMokkeryProjection<T>(
    private val builder: (MokkeryOption<*>) -> T
) : MokkeryOptionProjection<T> {
    private val store = mutableMapOf<MokkeryOption<*>, T>()
    override fun project(option: MokkeryOption<*>): T = synchronized(store) {
        store.getOrPut(option) { builder(option) }
    }
}

@Suppress("UNCHECKED_CAST")
@InternalMokkeryApi
public fun <P> MokkeryOption<*>.get(projection: MokkeryOptionProjection<P>): P = projection.project(this)
