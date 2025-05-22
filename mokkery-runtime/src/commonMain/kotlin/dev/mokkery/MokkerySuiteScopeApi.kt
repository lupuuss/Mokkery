package dev.mokkery

import dev.mokkery.context.require
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.utils.instances

/**
 * Returns all mocks from this [MokkerySuiteScope].
 */
public val MokkerySuiteScope.mocks: List<Any>
    get() = mokkeryContext
        .require(MocksRegistry)
        .mocks
        .instances
        .toList()
