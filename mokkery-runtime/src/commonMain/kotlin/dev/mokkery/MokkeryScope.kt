package dev.mokkery

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.MokkeryTools

/**
 * Base interface for all scopes that are based on [MokkeryContext].
 * Provides a set of operations that are available in all contexts.
 */
public interface MokkeryScope {

    public val mokkeryContext: MokkeryContext

    public companion object {

        @InternalMokkeryApi
        public val global: MokkeryScope = MokkeryScope(MokkeryTools.default())
    }
}

internal fun MokkeryScope(context: MokkeryContext): MokkeryScope = object : MokkeryScope {

    override val mokkeryContext = context

    override fun toString(): String = "MokkeryScope(mokkeryContext=$mokkeryContext)"
}
