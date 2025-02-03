@file:Suppress("UNUSED_PARAMETER")

package dev.mokkery

import dev.mokkery.internal.MokkeryPluginNotAppliedException

/**
 * Provides mock implementation of given type [T].
 *
 * [T] **must** be provided directly and **cannot** be a generic parameter.
 *
 * Currently supported types:
 * * Interfaces (not sealed)
 * * Function types
 * * Abstract/open classes with all methods/properties open/abstract and no-args constructor.
 */
public inline fun <reified T : Any> mock(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: T.() -> Unit = { }
): T = throw MokkeryPluginNotAppliedException()


/**
 * Provides mock implementation of given type [T] and registers it in this [MokkeryTestsScope].
 *
 * [T] **must** be provided directly and **cannot** be a generic parameter.
 *
 * Currently supported types:
 * * Interfaces (not sealed)
 * * Function types
 * * Abstract/open classes with all methods/properties open/abstract and no-args constructor.
 */
public inline fun <reified T : Any> MokkeryTestsScope.mock(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: T.() -> Unit = { }
): T = throw MokkeryPluginNotAppliedException()
