@file:Suppress("UNUSED_PARAMETER", "unused")

package dev.mokkery

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.MutablePropertyAnsweringScope
import dev.mokkery.answering.PropertyAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.mokkeryIntrinsic
import dev.mokkery.templating.MokkeryTemplatingScope
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 *
 * Starts defining an answer for a **single** regular function call specified by [block]. Returned scope allows defining answer.
 *
 * Provided [block] **must** be a lambda and single mock call **must** occur directly inside it. Extracting [block]
 * content to functions is prohibited.
 */
public fun <T> every(
    block: @Templating MokkeryTemplatingScope.() -> T
): BlockingAnsweringScope<T> = mokkeryIntrinsic

/**
 *
 * Starts defining an answer for a **single** suspend function call specified by [block]. Returned scope allows defining answer.
 *
 * Provided [block] **must** be a lambda and single mock call **must** occur directly inside it. Extracting [block]
 * content to functions is prohibited.
 */
public fun <T> everySuspend(
    block: @Templating suspend MokkeryTemplatingScope.() -> T
): SuspendAnsweringScope<T> = mokkeryIntrinsic

/**
 *
 * Starts defining an answer for a regular [function] call with any arguments.
 * Returned scope allows defining answer.
 *
 * Given [function] **must** be a regular (not suspend) function reference bound to a mock object.
 */
public fun <T> every(function: KFunction<T>): BlockingAnsweringScope<T> = mokkeryIntrinsic

/**
 *
 * Starts defining an answer for a suspend [function] call with any arguments.
 * Returned scope allows defining answer.
 *
 * Given [function] **must** be a suspend function reference bound to a mock object.
 */
public fun <T> everySuspend(function: KFunction<T>): SuspendAnsweringScope<T> = mokkeryIntrinsic

/**
 * Starts defining an answer for a read-only [property].
 * Returned scope allows defining answer.
 *
 * Given [property] **must** be a member property reference bound to a mock object.
 */
public fun <T> every(property: KProperty<T>): PropertyAnsweringScope<T> = mokkeryIntrinsic

/**
 * Starts defining answers for a mutable [property].
 * Returned scope allows defining answer.
 *
 * Given [property] **must** be a member property reference bound to a mock object.
 */
public fun <T> every(property: KMutableProperty<T>): MutablePropertyAnsweringScope<T> = mokkeryIntrinsic

/**
 * Starts defining answers for a mutable [property].
 *
 * Given [block] allows accessing [MutablePropertyAnsweringScope]
 *
 * Example:
 * ```kotlin
 * every(dependency::value) {
 *     getter returns 1
 *     setter throws IllegalStateException()
 * }
 *
 * dependency.value     // returns 1
 * dependency.value = 2 // throws IllegalStateException
 * ```
 *
 * Given [property] **must** be a member property reference bound to a mock object.
 */
public fun <T> every(
    property: KMutableProperty<T>,
    block: MutablePropertyAnsweringScope<T>.() -> Unit
) { mokkeryIntrinsic }
