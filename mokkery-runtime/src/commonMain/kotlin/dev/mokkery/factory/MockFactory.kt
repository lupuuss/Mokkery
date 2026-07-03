@file:Suppress("UnusedReceiverParameter", "unused")

package dev.mokkery.factory

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.configurer.MokkeryMockConfigurer
import dev.mokkery.configurer.plusAssign
import dev.mokkery.factory.configurer.MockFactoryConfigurer
import dev.mokkery.internal.context.settings
import dev.mokkery.internal.mokkeryIntrinsic
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Returns a [MockFactory] that is able to create mocks of given [classes].
 *
 * Unlike [dev.mokkery.mock], which requires the mocked type to be known at compile time, the returned factory
 * creates mocks from a [KType] resolved at runtime, as long as it matches one of [classes].
 *
 * Each element of [classes] **must** be a class literal (e.g. `Foo::class`) and its type must be supported
 * by [dev.mokkery.mock].
 */
public fun mockFactoryOf(
    vararg classes: KClass<*>,
    block: MockFactoryConfigurer.Block = { }
): MockFactory = mokkeryIntrinsic

/**
 * Returns a [MockFactory] that is able to create mocks of given [classes].
 * Every mock created by the returned factory is a child of given [MokkerySuiteScope].
 *
 * Unlike [dev.mokkery.mock], which requires the mocked type to be known at compile time, the returned factory
 * creates mocks from a [KType] resolved at runtime, as long as it matches one of [classes].
 *
 * Each element of [classes] **must** be a class literal (e.g. `Foo::class`) and its type must be supported
 * by [dev.mokkery.mock].
 */
public fun MokkerySuiteScope.mockFactoryOf(
    vararg classes: KClass<*>,
    block: MockFactoryConfigurer.Block = { }
): MockFactory = mokkeryIntrinsic

/**
 * Sets default [MockMode] for currently configured [MockFactory].
 */
public var MockFactoryConfigurer.defaultMockMode: MockMode
    get() = settings.defaultMockMode
    set(value) {
        this += settings.copy(defaultMockMode = value)
    }

/**
 * Creates a mock implementation for given type.
 *
 * Unlike [dev.mokkery.mock], which requires the mocked type to be known at compile time, a factory creates mocks
 * from a [KType] resolved at runtime.
 *
 * Use [mockFactoryOf] to create an instance of it.
 */
public interface MockFactory {

    /**
     * Creates a mock of given [type] or `null` if [type] is not supported by this factory.
     *
     * If it's possible to create a mock with given [type], then configuration [block] is applied to created instance.
     **/
    public fun createOrNull(type: KType, block: MokkeryMockConfigurer.Block<Any> = { }): Any?

    /**
     * Copies a factory with given configuration [block] applied.
     *
     * If [newScope] is provided, then original factory configuration is rejected.
     * Only ability to create mocks of specified types is preserved.
     * New configuration is based on [newScope] and [block] only.
     */
    @DelicateMokkeryApi
    public fun copy(newScope: MokkeryScope? = null, block: MockFactoryConfigurer.Block = { }): MockFactory
}

/**
 * Creates a mock of given [T] and applies the [block] or returns `null` if [T] is not supported by this factory.
 */
public inline fun <reified T : Any> MockFactory.createOrNull(
    noinline block: MokkeryMockConfigurer.Block<T> = { }
): T? {
    val mock = createOrNull(typeOf<T>(), block.unsafeCast()) ?: return null
    return mock as T
}

/**
 * Creates a mock of given [type] and applies the [block] or fails if [type] is not supported by this factory.
 *
 * */
public fun MockFactory.create(
    type: KType,
    block: MokkeryMockConfigurer.Block<Any> = { },
): Any = createOrNull(type, block) ?: mokkeryRuntimeError("Unable to create mock of $type using $this")

/**
 * Creates a mock of given [T] and applies the [block] or fails if [T] is not supported by this factory.
 */
public inline fun <reified T : Any> MockFactory.create(
    noinline block: MokkeryMockConfigurer.Block<T> = { },
): T = (create(typeOf<T>(), block.unsafeCast()) as T)

/**
 * Returns a [MockFactory] that combines this factory with [other].
 */
public operator fun MockFactory.plus(other: MockFactory): MockFactory = CombinedMockFactory(this, other)

private class CombinedMockFactory(
    private val left: MockFactory,
    private val right: MockFactory
): MockFactory {

    override fun createOrNull(
        type: KType,
        block: MokkeryMockConfigurer.Block<Any>
    ): Any? = left.createOrNull(type, block) ?: right.createOrNull(type, block)

    override fun copy(newScope: MokkeryScope?, block: MockFactoryConfigurer.Block): MockFactory {
        return CombinedMockFactory(left.copy(newScope, block), right.copy(newScope, block))
    }
}
