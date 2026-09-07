package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.toList

@Suppress("UNCHECKED_CAST")
internal class KeepOnTopContext(
    private val rest: MokkeryContext,
    private val top: MokkeryContext.Element,
) : MokkeryContext {

    override fun <T : MokkeryContext.Element> get(key: MokkeryContext.Key<T>): T? {
        if (top.key == key) return top as T
        return rest[key]
    }

    override fun <T> fold(initial: T, operation: (T, MokkeryContext.Element) -> T): T {
        return top.fold(rest.fold(initial, operation), operation)
    }

    override fun minus(key: MokkeryContext.Key<*>): MokkeryContext {
        if (top.key == key) return rest
        val newRest = rest - key
        // identity matters - `plus` skips rebuilding when `minus` reports nothing was removed
        return if (newRest === rest) this else KeepOnTopContext(newRest, top)
    }

    override fun plus(context: MokkeryContext): MokkeryContext = when (context) {
        MokkeryContext.Empty -> this
        is MokkeryContext.Element -> when (context.key) {
            top.key -> KeepOnTopContext(rest, context)
            else -> KeepOnTopContext(rest + context, top)
        }
        else -> when (val newTop = context[top.key]) {
            null -> KeepOnTopContext(rest + context, top)
            else -> KeepOnTopContext(rest + (context - top.key), newTop)
        }
    }

    override fun toString(): String = toList().toString()
}
