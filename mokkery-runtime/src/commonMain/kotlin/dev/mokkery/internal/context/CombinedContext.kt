package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext

internal class CombinedContext(
    private val left: MokkeryContext,
    private val element: MokkeryContext.Element
) : MokkeryContext {

    override fun <T : MokkeryContext.Element> get(key: MokkeryContext.Key<T>): T? {
        var cur = this
        while (true) {
            cur.element[key]?.let { return it }
            val next = cur.left
            if (next is CombinedContext) {
                cur = next
            } else {
                return next[key]
            }
        }
    }

    override fun <T> fold(
        initial: T,
        operation: (T, MokkeryContext.Element) -> T
    ): T = operation(left.fold(initial, operation), element)

    override fun minus(key: MokkeryContext.Key<*>): MokkeryContext {
        element[key]?.let { return left }
        val newLeft = left - key
        return when {
            newLeft === left -> this
            newLeft === MokkeryContext.Empty -> element
            else -> CombinedContext(newLeft, element)
        }
    }

    override fun toString() = buildString {
        append("[")
        val content = this@CombinedContext.fold("") { acc, element ->
            if (acc.isEmpty()) element.toString() else "$acc, $element"
        }
        append(content)
        append("]")
    }
}

