@file:Suppress("unused", "UnusedReceiverParameter")

package dev.mokkery

import dev.mokkery.internal.utils.mokkeryIntrinsic

/**
 * Returns given [obj] wrapped with a spying implementation of [T].
 *
 * [T] must be provided directly and cannot be a generic parameter.
 *
 * Currently supported types:
 * * interfaces (not sealed)
 * * function types
 * * Abstract/open classes.
 */
@Suppress("UNUSED_PARAMETER")
public fun <T : Any> spy(
    obj: T,
    block: T.() -> Unit = { }
): T = mokkeryIntrinsic


/**
 * Returns given [obj] wrapped with a spying implementation of [T]. It is a child of given [MokkerySuiteScope].
 *
 * [T] **must** be provided directly and **cannot** be a generic parameter.
 *
 * Currently supported types:
 * * Interfaces (not sealed)
 * * Function types
 * * Abstract/open classes.
 */
public fun <T : Any> MokkerySuiteScope.spy(
    obj: T,
    block: T.() -> Unit = { }
): T = mokkeryIntrinsic

