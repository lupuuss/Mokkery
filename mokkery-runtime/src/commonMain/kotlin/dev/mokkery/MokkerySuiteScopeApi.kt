package dev.mokkery

import dev.mokkery.context.require
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.instances

/**
 * Returns all mocks from this [MokkerySuiteScope].
 */
public val MokkerySuiteScope.mocks: List<Any>
    get() = mokkeryContext
        .require(MokkeryInstancesRegistry)
        .collection
        .instances
        .toList()
