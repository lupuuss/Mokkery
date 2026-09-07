@file:Suppress("UnusedReceiverParameter", "unused")

package dev.mokkery.factory

import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.configurer.MokkerySpyConfigurer
import dev.mokkery.factory.configurer.SpyFactoryConfigurer
import dev.mokkery.internal.mokkeryIntrinsic
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Returns a [SpyFactory] that is able to create spies of given [classes].
 *
 * Unlike [dev.mokkery.spy], which requires the spied type to be known at compile time, the returned factory
 * creates spies from a [KType] resolved at runtime, as long as it matches one of [classes].
 *
 * Each element of [classes] **must** be a class literal (e.g. `Foo::class`) and its type must be supported
 * by [dev.mokkery.spy].
 */
public fun spyFactoryOf(
    vararg classes: KClass<*>,
    block: SpyFactoryConfigurer.Block = { },
): SpyFactory = mokkeryIntrinsic

/**
 * Returns a [SpyFactory] that is able to create spies of given [classes].
 * Every spy created by the returned factory is a child of given [MokkerySuiteScope].
 *
 * Unlike [dev.mokkery.spy], which requires the spied type to be known at compile time, the returned factory
 * creates spies from a [KType] resolved at runtime, as long as it matches one of [classes].
 *
 * Each element of [classes] **must** be a class literal (e.g. `Foo::class`) and its type must be supported
 * by [dev.mokkery.spy].
 */
public fun MokkerySuiteScope.spyFactoryOf(
    vararg classes: KClass<*>,
    block: SpyFactoryConfigurer.Block = { },
): SpyFactory = mokkeryIntrinsic

/**
 * Creates a spy implementation for given type.
 *
 * Unlike [dev.mokkery.spy], which requires the spied type to be known at compile time, a factory creates spies
 * from a [KType] resolved at runtime.
 *
 * Use [spyFactoryOf] to create an instance of it.
 */
public interface SpyFactory {

    /**
     * Creates a spy of given [type] wrapping [obj] or `null` if [type] is not supported by this factory.
     *
     * [obj] **must** be an instance of given [type].
     */
    public fun createOrNull(type: KType, obj: Any, block: MokkerySpyConfigurer.Block<Any> = { }): Any?

    /**
     * Copies this factory with applied [block].
     *
     * If [newScope] is provided, the copy derives its context from it instead of the scope of this factory.
     */
    public fun copy(newScope: MokkeryScope? = null, block: SpyFactoryConfigurer.Block = { }): SpyFactory
}

/**
 * Creates a spy of given [T] wrapping [obj] and applies the [block] or returns `null`
 * if [T] is not supported by this factory.
 */
public inline fun <reified T : Any> SpyFactory.createOrNull(
    obj: T,
    noinline block: MokkerySpyConfigurer.Block<T> = { },
): T? {
    val spy = createOrNull(typeOf<T>(), obj, block.unsafeCast()) ?: return null
    return spy as T
}

/**
 * Creates a spy of given [type] wrapping [obj] or fails if [type] is not supported by this factory.
 *
 * [obj] **must** be an instance of given [type].
 */
public fun SpyFactory.create(
    type: KType,
    obj: Any,
    block: MokkerySpyConfigurer.Block<Any> = { },
): Any = createOrNull(type, obj, block) ?: mokkeryRuntimeError("Unable to create spy of $type using $this")

/**
 * Creates a spy of given type [T] wrapping [obj] or fails if [T] is not supported by this factory.
 *
 * @param block is applied to created spy
 */
public inline fun <reified T : Any> SpyFactory.create(
    obj: T,
    noinline block: MokkerySpyConfigurer.Block<T> = { },
): T = create(typeOf<T>(), obj, block.unsafeCast()) as T

/**
 * Returns a [SpyFactory] that combines this factory with [other].
 */
public operator fun SpyFactory.plus(other: SpyFactory): SpyFactory = CombinedSpyFactory(this, other)

private class CombinedSpyFactory(
    private val left: SpyFactory,
    private val right: SpyFactory
) : SpyFactory {

    override fun createOrNull(
        type: KType,
        obj: Any,
        block: MokkerySpyConfigurer.Block<Any>
    ): Any? = left.createOrNull(type, obj, block) ?: right.createOrNull(type, obj, block)

    override fun copy(newScope: MokkeryScope?, block: SpyFactoryConfigurer.Block): SpyFactory {
        return CombinedSpyFactory(left.copy(newScope, block), right.copy(newScope, block))
    }
}
