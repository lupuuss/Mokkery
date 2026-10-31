package dev.mokkery.internal.answering

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.answering.MokkeryField
import dev.mokkery.answering.MokkeryMutableProperty
import dev.mokkery.answering.MokkeryProperty
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import dev.mokkery.rendering.descriptionRenderer
import dev.mokkery.rendering.renderOrToString
import dev.mokkery.self
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

internal fun <T> ReadOnlyProperty<Any?, T>.asMokkeryProperty(): MokkeryProperty<T> {
    val delegate = this
    return object : MokkeryProperty<T>, Renderable {
        override fun getValue(
            scope: MokkeryBlockingCallScope,
            property: KProperty<*>
        ): T = delegate.getValue(scope.self, property)

        context(scope: MokkeryRenderingScope)
        override fun render(): String = delegate.renderOrToString()
    }
}

internal fun <T> ReadWriteProperty<Any?, T>.asMokkeryProperty(): MokkeryMutableProperty<T> {
    val delegate = this
    return object : MokkeryMutableProperty<T>, Renderable {

        override fun getValue(
            scope: MokkeryBlockingCallScope,
            property: KProperty<*>
        ): T = delegate.getValue(scope.self, property)

        override fun setValue(
            scope: MokkeryBlockingCallScope,
            property: KProperty<*>,
            value: T
        ) = delegate.setValue(scope.self, property, value)

        context(scope: MokkeryRenderingScope)
        override fun render(): String = delegate.renderOrToString()
    }
}

internal class BoundProperty<T>(
    private val property: KProperty0<T>,
) : MokkeryProperty<T>, Renderable {
    override fun getValue(scope: MokkeryBlockingCallScope, property: KProperty<*>): T = this.property.get()

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "bound(${property.name})"
}

internal class BoundMutableProperty<T>(
    private val property: KMutableProperty0<T>,
) : MokkeryMutableProperty<T>, Renderable {
    override fun getValue(scope: MokkeryBlockingCallScope, property: KProperty<*>): T = this.property.get()

    override fun setValue(scope: MokkeryBlockingCallScope, property: KProperty<*>, value: T) {
        this.property.set(value)
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "bound(${property.name})"
}

internal class DefaultMokkeryField<T>(
    initial: T,
    private val capture: Capture<T>,
) : MokkeryField<T>, Renderable {

    private val lock = reentrantLock()
    private var _value by atomic(initial)
    override var value: T
        get() = _value
        set(value) {
            _value = value
        }

    override fun getValue(scope: MokkeryBlockingCallScope, property: KProperty<*>): T = value

    override fun setValue(
        scope: MokkeryBlockingCallScope,
        property: KProperty<*>,
        value: T
    ) {
        lock.withLock {
            this.value = value
            capture.capture(value)
        }
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "field(value=${scope.descriptionRenderer.render(value)})"
}
