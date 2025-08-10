@file:Suppress("UNUSED_PARAMETER", "UnusedReceiverParameter")

package dev.mokkery

import dev.mokkery.internal.utils.toBeReplacedByCompilerPlugin

/**
 * Provides mock implementation of given type [T].
 *
 * [T] **must** be provided directly and **cannot** be a generic parameter.

 * Currently supported types:
 * * interfaces (not sealed)
 * * function types
 * * Abstract/open classes.
 */
public inline fun <reified T : Any> mock(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: T.() -> Unit = { }
): T = toBeReplacedByCompilerPlugin


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
public inline fun <reified T : Any> MokkerySuiteScope.mock(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: T.() -> Unit = { }
): T = toBeReplacedByCompilerPlugin
