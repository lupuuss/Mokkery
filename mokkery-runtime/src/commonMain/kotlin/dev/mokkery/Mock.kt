@file:Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")

package dev.mokkery

import dev.mokkery.internal.mokkeryIntrinsic

/**
 * Provides mock implementation of given type [T].
 *
 * [T] **must** be provided directly and **cannot** be a generic parameter.

 * Currently supported types:
 * * interfaces (not sealed)
 * * function types
 * * Abstract/open classes.
 *
 * @param mode determines the default behavior when no answer is provided. If not provided, default mode is used.
 */
public fun <T : Any> mock(
    mode: MockMode? = null,
    block: T.() -> Unit = { }
): T = mokkeryIntrinsic

/**
 * Provides mock implementation of given type [T]. It is a child of given [MokkerySuiteScope].
 *
 * [T] **must** be provided directly and **cannot** be a generic parameter.
 *
 * Currently supported types:
 * * interfaces (not sealed)
 * * function types
 * * Abstract/open classes.
 */
public fun <T : Any> MokkerySuiteScope.mock(
    mode: MockMode? = null,
    block: T.() -> Unit = { }
): T = mokkeryIntrinsic
