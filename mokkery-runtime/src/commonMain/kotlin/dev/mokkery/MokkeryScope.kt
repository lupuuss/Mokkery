package dev.mokkery

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.MokkeryTools

/**
 * Base interface for all scopes that are based on [MokkeryContext].
 * Provides a set of operations that are available in all contexts.
 */
public interface MokkeryScope {

    public val mokkeryContext: MokkeryContext

    public companion object {

        internal val global: MokkeryScope = object : MokkeryScope {

            override val mokkeryContext: MokkeryContext = MokkeryTools.default()

            override fun toString(): String = "MokkeryScope.global(mokkeryContext=$mokkeryContext)"
        }
    }
}
