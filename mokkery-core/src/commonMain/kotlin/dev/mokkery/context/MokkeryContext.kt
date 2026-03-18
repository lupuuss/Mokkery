package dev.mokkery.context

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.context.CombinedContext
import dev.mokkery.internal.context.MemoizedContext

/**
 *  A set of [MokkeryContext.Element]s.
 *  It's used to provide any information/dependency Mokkery machinery.
 *  It works in the same way as [kotlin.coroutines.CoroutineContext].
 */
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

/**
 * Returns an element for a given [key] if present. Throws [MokkeryRuntimeException] otherwise.
 */
public fun <T : MokkeryContext.Element> MokkeryContext.require(key: MokkeryContext.Key<T>): T {
    return get(key) ?: throw MokkeryRuntimeException("Element for key = $key is required, but not found in the context!")
}

@InternalMokkeryApi
public fun MokkeryContext.memoized(): MokkeryContext = MemoizedContext(this)

@InternalMokkeryApi
public inline fun MokkeryContext.forEach(crossinline block: (MokkeryContext.Element) -> Unit) {
    fold(Unit) { _, element -> block(element) }
}

@InternalMokkeryApi
public fun MokkeryContext.toMap(): Map<MokkeryContext.Key<*>, MokkeryContext.Element> {
    val map = LinkedHashMap<MokkeryContext.Key<*>, MokkeryContext.Element>()
    forEach { map[it.key] = it }
    return map
}

@InternalMokkeryApi
public fun <T : MutableCollection<MokkeryContext.Element>> MokkeryContext.toCollection(collection: T): T {
    forEach(collection::add)
    return collection
}

@InternalMokkeryApi
public fun MokkeryContext.toList(): List<MokkeryContext.Element> = toCollection(ArrayList())
