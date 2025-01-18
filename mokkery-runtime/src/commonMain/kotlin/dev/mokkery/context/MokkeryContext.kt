package dev.mokkery.context

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.context.CombinedContext
import dev.mokkery.internal.utils.mokkeryRuntimeError

/**
 *  A set of [MokkeryContext.Element]s.
 *  It's used to provide any information/dependency required for Mokkery interceptors.
 *  It works in the same way as [kotlin.coroutines.CoroutineContext].
 */
@DelicateMokkeryApi
public interface MokkeryContext {

    public operator fun <T : Element> get(key: Key<T>): T?

    public operator fun plus(
        context: MokkeryContext
    ): MokkeryContext = when {
        context === Empty -> this
        else -> context.fold(this) { acc, element ->
            val removed = acc - element.key
            if (removed === Empty) {
                element
            } else {
                CombinedContext(removed, element)
            }
        }
    }

    public fun <T> fold(initial: T, operation: (T, Element) -> T): T

    public operator fun minus(key: Key<*>): MokkeryContext

    public interface Element : MokkeryContext {

        public val key: Key<*>

        @Suppress("UNCHECKED_CAST")
        override fun <T : Element> get(key: Key<T>): T? = if (this.key == key) this as T else null

        override fun <T> fold(initial: T, operation: (T, Element) -> T): T = operation(initial, this)

        override fun minus(key: Key<*>): MokkeryContext = if (this.key == key) Empty else this
    }

    public interface Key<T : Element>

    public object Empty : MokkeryContext {

        override fun <T : Element> get(key: Key<T>): T? = null

        override fun plus(context: MokkeryContext): MokkeryContext = context

        override fun <T> fold(initial: T, operation: (T, Element) -> T): T = initial

        override fun minus(key: Key<*>): MokkeryContext = this

        override fun toString(): String = "MokkeryContext.Empty"
    }
}

internal fun <T : MokkeryContext.Element> MokkeryContext.require(key: MokkeryContext.Key<T>): T {
    return get(key) ?: mokkeryRuntimeError("Element for key = $key is required, but not found in the context!")
}
