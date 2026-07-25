package dev.mokkery

import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.internal.context.Settings
import dev.mokkery.internal.interceptor.ForkedMokkeryCallHooks
import dev.mokkery.internal.mokkeryIntrinsic
import dev.mokkery.internal.requireInstanceScope

/**
 * Base interface for all scopes that are based on [MokkeryContext].
 * Provides a set of operations that are available in all contexts.
 */
public interface MokkeryScope {

    public val mokkeryContext: MokkeryContext

    public companion object {

        /**
         * The root [MokkeryScope] that all other scopes derive from.
         */
        public val global: MokkeryScope = MokkeryScope(
            MokkeryTools.default()
                    + Settings.default()
                    + ForkedMokkeryCallHooks()
        )

        /**
         *  Returns the [MokkeryScope] associated with current file.
         *
         *  In principle all scopes in given file should derive from it. It's propagated by the compiler plugin.
         */
        @InternalMokkeryApi
        public val file: MokkeryScope get() = mokkeryIntrinsic

        /**
         * Returns the [MokkeryInstanceScope] associated with the given [mock].
         *
         * @throws MokkeryRuntimeException if [mock] is not a Mokkery instance.
         */
        public fun from(mock: Any): MokkeryInstanceScope = mock.requireInstanceScope()
    }
}

internal fun MokkeryScope(context: MokkeryContext): MokkeryScope = object : MokkeryScope {

    override val mokkeryContext = context

    override fun toString(): String = "MokkeryScope(mokkeryContext=$mokkeryContext)"
}
