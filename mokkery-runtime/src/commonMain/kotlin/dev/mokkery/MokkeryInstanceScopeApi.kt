package dev.mokkery

import dev.mokkery.configurer.MokkeryMockConfigurer
import dev.mokkery.configurer.configurer
import dev.mokkery.configurer.plusAssign
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
 * Controls [MockMode] for mock currently configured by [this] configurer.
 */
public var MokkeryMockConfigurer.mockMode: MockMode
    get() = instanceSpec.requireMock().mode
    set(value) {
        this += instanceSpec.requireMock().copy(mode = value)
    }

/**
 * Controls [MockMode] for [this] mock.
 */
context(aware: MokkeryMockConfigurer.Aware<T>)
public var <T : Any> T.mockMode: MockMode
    get() = configurer.mockMode
    set(value) {
        configurer.mockMode = value
    }

/**
 * Returns reference spied by the spy associated with this scope.
 */
public val MokkerySpyScope.spiedObject: Any
    get() = instanceSpec.requireSpy().spiedObject

/**
 * Returns [MokkerySpyScope.spiedObject] as [T].
 */
public inline fun <reified T : Any> MokkerySpyScope.spiedObject(): T = spiedObject as T
