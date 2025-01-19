package dev.mokkery

import dev.mokkery.context.MokkeryContext

/**
 * Base interface for all scopes that are based on [MokkeryContext].
 * Provides a set of operations that are available in all contexts.
 */
public interface MokkeryScope {

    public val mokkeryContext: MokkeryContext
}
