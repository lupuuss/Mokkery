package dev.mokkery.answering

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.internal.answering.BoundMutableProperty
import dev.mokkery.internal.answering.BoundProperty
import dev.mokkery.internal.answering.DefaultMokkeryField
import dev.mokkery.matcher.capture.Capture
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

/**
 * A property to which Mokkery might delegate mocked properties. It can be registered with [by].
 * This interface is meant for read-only properties. Mocked mutable properties can be delegated only to
 * [MokkeryMutableProperty].
 *
 * Implement [dev.mokkery.rendering.Renderable] to control how this property is presented in Mokkery messages.
 * Otherwise, [Any.toString] is used.
 *
 * Built-in properties can be found in [MokkeryProperty.Companion] e.g. [MokkeryProperty.Companion.field].
 */
public interface MokkeryProperty<out T> {

    /**
     * Returns the value for a read of the mocked [property].
     */
    public fun getValue(scope: MokkeryBlockingCallScope, property: KProperty<*>): T

    public companion object {

        /**
         * A [MokkeryField] that starts with [initial] value. Each value written through the mocked property's
         * setter is passed to [capture], in the order the writes are applied (e.g. use [Capture.container]
         * to record write history). Changes made directly to [MokkeryField.value] are not captured.
         */
        public fun <T> field(
            initial: T,
            capture: Capture<T> = Capture.void(),
        ): MokkeryField<T> = DefaultMokkeryField(initial, capture)

        /**
         * A [MokkeryProperty] that reads from [property] each time the mocked property is read.
         */
        public fun <T> bound(property: KProperty0<T>): MokkeryProperty<T> = BoundProperty(property)

        /**
         * A [MokkeryMutableProperty] that reads from and writes to [property] each time the mocked property is accessed.
         */
        public fun <T> bound(property: KMutableProperty0<T>): MokkeryMutableProperty<T> = BoundMutableProperty(property)
    }
}

/**
 * A mutable property to which Mokkery might delegate mocked properties. It can be registered with [by].
 *
 * Built-in properties can be found in [MokkeryProperty.Companion] e.g. [MokkeryProperty.Companion.field].
 */
public interface MokkeryMutableProperty<T> : MokkeryProperty<T> {

    /**
     * Handles a write of [value] to the mocked [property].
     */
    public fun setValue(scope: MokkeryBlockingCallScope, property: KProperty<*>, value: T): Unit
}

/**
 * A [MokkeryMutableProperty] that stores the mocked property's value like a backing field.
 * Its default implementation is created with [MokkeryProperty.Companion.field].
 */
public interface MokkeryField<T> : MokkeryMutableProperty<T> {

    /**
     * Gives access to backing field of this [MokkeryField].
     */
    public var value: T
}
