package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.toList

internal fun memoizedContext(
    fallback: MokkeryContext,
    memoized: Map<MokkeryContext.Key<*>, MokkeryContext.Element>
): MokkeryContext = when {
    memoized.isEmpty() -> fallback
    fallback === MokkeryContext.Empty && memoized.size == 1 -> memoized.values.single()
    else -> MemoizedContext(fallback, memoized)
}

private class MemoizedContext(
    private val fallback: MokkeryContext,
    private val memoized: Map<MokkeryContext.Key<*>, MokkeryContext.Element>,
) : MokkeryContext {

    override fun <T : MokkeryContext.Element> get(key: MokkeryContext.Key<T>): T? {
        val element = memoized[key] ?: return fallback[key]
        @Suppress("UNCHECKED_CAST")
        return element as? T
    }

    override fun <T> fold(initial: T, operation: (T, MokkeryContext.Element) -> T): T {
        val inherited = fallback.fold(initial) { acc, element ->
            if (element.key in memoized) acc else operation(acc, element)
        }
        return memoized.values.fold(inherited, operation)
    }

    override fun minus(key: MokkeryContext.Key<*>): MokkeryContext {
        val fallback = fallback - key
        return when {
            key in memoized -> memoizedContext(fallback, memoized - key)
            fallback === this.fallback -> this
            else -> memoizedContext(fallback, memoized)
        }
    }

    override fun toString(): String = toList().toString()
}
