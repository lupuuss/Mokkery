@file:Suppress("unused", "UnusedReceiverParameter")

package dev.mokkery

import dev.mokkery.internal.utils.toBeReplacedByCompilerPlugin

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
public inline fun <reified T : Any> spy(
    obj: T,
    block: T.() -> Unit = { }
): T = toBeReplacedByCompilerPlugin


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
public inline fun <reified T : Any> MokkerySuiteScope.spy(
    obj: T,
    block: T.() -> Unit = { }
): T = toBeReplacedByCompilerPlugin

