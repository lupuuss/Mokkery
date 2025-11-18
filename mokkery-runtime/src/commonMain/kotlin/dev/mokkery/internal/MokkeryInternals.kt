package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.context.tools
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
