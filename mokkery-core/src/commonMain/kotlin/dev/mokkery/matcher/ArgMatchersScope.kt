package dev.mokkery.matcher

import dev.mokkery.internal.answer.defaultValue
import dev.mokkery.internal.templating.TemplatingContext
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
public class ArgMatchersScope internal constructor(private val registry: TemplatingContext) {

    public inline fun <reified T> any(): T = any(T::class)

    public inline fun <reified T> matching(noinline predicate: (T) -> Boolean): T = matching(T::class, predicate)

    public fun <T> eq(value: T): T {
        registry.registerMatcher(EqMatcher(value))
        return value?.let { defaultValue<T>(it::class) } as T
    }

    @PublishedApi
    internal fun <T> any(cls: KClass<*>): T {
        registry.registerMatcher(AnyMatcher(cls))
        return defaultValue(cls)
    }

    @PublishedApi
    internal fun <T> matching(cls: KClass<*>, block: (T) -> Boolean): T {
        registry.registerMatcher(CustomMatcher(cls) { block(it as T) })
        return defaultValue(cls)
    }
}


