@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.plugin.core.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.plugin.core.MokkeryPluginScope
import kotlin.reflect.KClass

inline fun <T : Any> T.asMokkeryContext(key: ValueContext.Key<T>): MokkeryContext = ValueContext(
    key = key,
    value = this,
)

inline fun <T : Any> MokkeryPluginScope.readValue(key: ValueContext.Key<T>): T = mokkeryContext.require(key).value

inline fun <reified T : Any> createValueKey() = ValueContext.Key(T::class)

data class ValueContext<T : Any>(
    override val key: Key<T>,
    val value: T
) : MokkeryContext.Element {

    data class Key<T : Any>(val klass: KClass<T>) : MokkeryContext.Key<ValueContext<T>>
}
