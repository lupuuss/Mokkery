package dev.mokkery

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
         * The [MokkeryScope] of the current module. Every scope created in the module without an explicit parent derives
         * from it, e.g. the scopes of mocks, spies and [MokkerySuiteScope]s.
         *
         * A module here is a Kotlin module: a set of Kotlin files compiled together, e.g. a Gradle source set, or a
         * compilation in Kotlin Multiplatform. In particular:
         * - Main and test sources are separate modules with separate scopes, even though tests can access
         *   main's `internal` declarations.
         * - In Kotlin Multiplatform, shared sources (e.g. `commonTest`) are compiled into every platform compilation
         *   (e.g. `jvmTest`, `jsTest`), so each platform has its own module scope.
         * - The scope is chosen by the module in which the code is compiled, not the one it is called from. A mock created
         *   by a helper function in main sources uses main's scope, even when the helper is called from tests.
         *
         * The scope derives from [global] and carries the module's default [MockMode] and [dev.mokkery.verify.VerifyMode]
         * configured in the Mokkery Gradle plugin. It is created lazily on first access, which also runs the module's
         * [dev.mokkery.annotations.MokkeryModuleInit] functions.
         */
        public val module: MokkeryScope
            get() = mokkeryIntrinsic

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
