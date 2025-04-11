package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.toMap

internal fun MokkeryContext.memoized(): MokkeryContext = MemoizedContext(this)

@Suppress("UNCHECKED_CAST")
private class MemoizedContext(private val originalContext: MokkeryContext): MokkeryContext {

    private val memoized = originalContext.toMap()

    override fun <T : MokkeryContext.Element> get(key: MokkeryContext.Key<T>): T? = memoized[key] as? T

    override fun <T> fold(initial: T, operation: (T, MokkeryContext.Element) -> T): T = memoized
        .values
        .fold(initial, operation)

    override fun minus(key: MokkeryContext.Key<*>): MokkeryContext = when {
        key in memoized -> originalContext
            .minus(key)
            .let { result ->
                when {
                    result === MokkeryContext.Empty -> result
                    else -> MemoizedContext(result)
                }
            }
        else -> this
    }

    override fun toString(): String = originalContext.toString()
}
