package dev.mokkery.internal

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.context.settings
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.rendering.renderingScope
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.verify.VerifyMode
import kotlin.jvm.JvmInline

/**
 * Wrapper class for [MokkeryScope] to expose some internal APIs without polluting actually public API.
 */
@InternalMokkeryApi
@JvmInline
public value class MokkeryInternals<out T : MokkeryScope>(public val scope: T)

/**
 * Wraps [MokkeryScope] with [MokkeryInternals] to expose some internal APIs without polluting actually public API.
 */
@InternalMokkeryApi
@Suppress("NOTHING_TO_INLINE")
public inline val <T : MokkeryScope> T.mokkeryInternals: MokkeryInternals<T>
    get() = MokkeryInternals(this)

/**
 * Resets internal mocks counter.
 */
@InternalMokkeryApi
public fun MokkeryInternals<*>.resetMocksCounter() {
    scope.tools.mocksCounter.reset()
}

@InternalMokkeryApi
public val MokkeryInternals<*>.renderingScope: MokkeryRenderingScope
    get() = scope.renderingScope()

@InternalMokkeryApi
public val MokkeryInternals<*>.defaultVerifyMode: VerifyMode
    get() = scope.settings.defaultVerifyMode

@InternalMokkeryApi
public val MokkeryInternals<*>.defaultMockMode: MockMode
    get() = scope.settings.defaultMockMode
