package dev.mokkery.answering

import dev.mokkery.internal.answering.MokkeryPropertyGetAnswer
import dev.mokkery.internal.answering.MokkeryPropertySetAnswer
import dev.mokkery.internal.answering.asMokkeryProperty
import dev.mokkery.internal.answering.kProperty
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty

/**
 * Property getter is delegated to [property] (e.g. [MokkeryProperty.Companion.field]).
 */
public infix fun <T> PropertyAnsweringScope<T>.by(property: MokkeryProperty<T>) {
    answers(MokkeryPropertyGetAnswer(property, kProperty))
}

/**
 * Property getter and setter are delegated to [property] (e.g. [MokkeryProperty.Companion.field]).
 */
public infix fun <T> MutablePropertyAnsweringScope<T>.by(property: MokkeryMutableProperty<T>) {
    getter.answers(MokkeryPropertyGetAnswer(property, kProperty))
    setter.answers(MokkeryPropertySetAnswer(property, kProperty))
}

/**
 * Property getter is delegated to [delegate], with the mock as `thisRef`.
 */
public infix fun <T> PropertyAnsweringScope<T>.by(
    delegate: ReadOnlyProperty<Any?, T>
): Unit = by(delegate.asMokkeryProperty())

/**
 * Property getter and setter are delegated to [delegate], with the mock as `thisRef`.
 */
public infix fun <T> MutablePropertyAnsweringScope<T>.by(
    delegate: ReadWriteProperty<Any?, T>
): Unit = by(delegate.asMokkeryProperty())

/**
 * Property getter always returns [value]. Setter ignores written values.
 */
public infix fun <T> MutablePropertyAnsweringScope<T>.returns(value: T) {
    getter returns value
    setter returns Unit
}

/**
 * Property getter returns value provided each time by [function]. Setter ignores written values.
 */
public infix fun <T> MutablePropertyAnsweringScope<T>.returnsBy(function: () -> T) {
    getter returnsBy function
    setter returns Unit
}

/**
 * Property getter and setter always throw [error].
 */
public infix fun <T> MutablePropertyAnsweringScope<T>.throws(error: Throwable) {
    getter throws error
    setter throws error
}

/**
 * Property getter and setter throw error provided each time by [function].
 */
public infix fun <T> MutablePropertyAnsweringScope<T>.throwsBy(function: () -> Throwable) {
    getter throwsBy function
    setter throwsBy function
}

/**
 * Property getter and setter throw [IllegalStateException] with given [message].
 */
public infix fun <T> MutablePropertyAnsweringScope<T>.throwsErrorWith(message: Any) {
    getter throwsErrorWith message
    setter throwsErrorWith message
}
