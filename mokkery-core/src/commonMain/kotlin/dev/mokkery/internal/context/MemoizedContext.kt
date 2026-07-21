package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.toMap

internal fun memoizedContext(context: MokkeryContext): MokkeryContext = memoizedContext(context.toMap())

internal fun memoizedContext(
    memoized: Map<MokkeryContext.Key<*>, MokkeryContext.Element>
): MokkeryContext = when (memoized.size) {
    0 -> MokkeryContext.Empty
    1 -> memoized.values.single()
    else -> object : MokkeryContext {

        @Suppress("UNCHECKED_CAST")
        override fun <T : MokkeryContext.Element> get(key: MokkeryContext.Key<T>): T? = memoized[key] as? T

        override fun <T> fold(initial: T, operation: (T, MokkeryContext.Element) -> T): T = memoized
            .values
            .fold(initial, operation)

        override fun minus(key: MokkeryContext.Key<*>): MokkeryContext = when {
            key in memoized -> memoizedContext(memoized - key)
            else -> this
        }

        override fun toString(): String = memoized.values.toString()
    }
}
