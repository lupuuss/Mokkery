package dev.mokkery.rendering

import dev.mokkery.context.MokkeryContext
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * Provides a human-readable representation for an instance of type T.
 */
public interface Renderer<in T> : MokkeryContext.Element {

    /**
     * Provides a human-readable representation for [value].
     */
    context(scope: MokkeryRenderingScope)
    public fun render(value: T): String

    public class Key<T>(private val name: String) : MokkeryContext.Key<Renderer<T>> {

        override fun toString(): String = "Renderer.Key(name='$name', ref=<${super.toString()}>)"
    }

    public companion object {

        public fun <T> key(): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Key<T>>> {
            return PropertyDelegateProvider { _, property ->
                val k = Key<T>(property.name)
                ReadOnlyProperty { _, _ -> k }
            }
        }
    }
}
