package dev.mokkery

import dev.mokkery.internal.MokkeryPluginNotAppliedException

/**
 * Returns given [obj] wrapped with a spying implementation of [T].
 *
 * [T] must be provided directly and cannot be a generic parameter.
 *
 * Currently supported types:
 * * interfaces (not sealed)
 * * function types
 * * Abstract/open classes with all methods/properties open/abstract and no-args constructor.
 */
@Suppress("UNUSED_PARAMETER")
public inline fun <reified T : Any> spy(
    obj: T,
    block: T.() -> Unit = { }
): T = throw MokkeryPluginNotAppliedException()


/**
 * Returns given [obj] wrapped with a spying implementation of [T]. It is a child of given [MokkerySuiteScope].
 *
 * [T] **must** be provided directly and **cannot** be a generic parameter.
 *
 * Currently supported types:
 * * Interfaces (not sealed)
 * * Function types
 * * Abstract/open classes with all methods/properties open/abstract and no-args constructor.
 */
public inline fun <reified T : Any> MokkerySuiteScope.spy(
    obj: T,
    block: T.() -> Unit = { }
): T = throw MokkeryPluginNotAppliedException()

