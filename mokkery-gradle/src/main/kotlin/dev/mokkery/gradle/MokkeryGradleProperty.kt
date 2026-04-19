package dev.mokkery.gradle

import dev.mokkery.annotations.InternalMokkeryApi
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

@InternalMokkeryApi
public interface MokkeryGradleProperty<T> {

    public fun get(): List<T>

    public fun convention(values: List<T>)

    public companion object {

        public fun <T : Any> from(
            property: Property<T>
        ): MokkeryGradleProperty<T> = object : MokkeryGradleProperty<T> {

            override fun get(): List<T> = listOfNotNull(property.orNull)

            override fun convention(values: List<T>) {
                values.singleOrNull()?.let {
                    property.convention(it)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        public fun <T : Any> fromAsAny(
            property: Property<T>
        ): MokkeryGradleProperty<Any> = from(property) as MokkeryGradleProperty<Any>

        public fun <T : Any> from(
            property: ListProperty<T>
        ): MokkeryGradleProperty<T> = object : MokkeryGradleProperty<T> {

            override fun get(): List<T> = property.orNull.orEmpty()

            override fun convention(values: List<T>) {
                if (values.isEmpty()) return
                property.convention(values)
            }
        }

        @Suppress("UNCHECKED_CAST")
        public fun <T : Any> fromAsAny(
            property: ListProperty<T>
        ): MokkeryGradleProperty<Any> = from(property) as MokkeryGradleProperty<Any>
    }
}
