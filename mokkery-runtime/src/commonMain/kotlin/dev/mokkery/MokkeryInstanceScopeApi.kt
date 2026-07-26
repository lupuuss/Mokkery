package dev.mokkery

import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.requireMock
import dev.mokkery.internal.context.requireSpy

/**
 * Returns [MockMode] of the mock associated with this scope.
 *
 * @throws MokkeryRuntimeException if this scope is associated with a spy.
 */
public val MokkeryMockScope.mockMode: MockMode
    get() = instanceSpec.requireMock().mode

/**
 * Returns reference spied by the spy associated with this scope.
 */
public val MokkerySpyScope.spiedObject: Any
    get() = instanceSpec.requireSpy().spiedObject

/**
 * Returns [MokkerySpyScope.spiedObject] as [T].
 */
public inline fun <reified T : Any> MokkerySpyScope.spiedObject(): T = spiedObject as T
