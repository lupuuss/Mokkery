package dev.mokkery.presets

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.configurer.MokkeryMockConfigurer
import dev.mokkery.configurer.MokkerySpyConfigurer
import dev.mokkery.factory.MockFactory
import dev.mokkery.factory.SpyFactory
import dev.mokkery.factory.configurer.MockFactoryConfigurer
import dev.mokkery.factory.configurer.SpyFactoryConfigurer
import dev.mokkery.internal.presets.presets
import dev.mokkery.internal.toMType
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Registers a configuration [block] that is applied to every mock of type [T] created by the configured [MockFactory].
 *
 * The [block] is applied separately to each created mock, before the block given to [MockFactory.createOrNull],
 * so the latter takes precedence.
 *
 * Presets are matched by the exact type - a preset registered for a supertype of a created mock is not applied.
 * Type arguments are matched by their classes and nullability is ignored. A star projection matches any type argument.
 *
 * Presets are cumulative - registering a preset for a type that already has one does not replace it and all
 * matching presets are applied. They are applied from the least to the most specific match, in the registration
 * order, with presets inherited from a copied factory first, so the most specific and the most recently registered
 * preset takes precedence.
 *
 * Registering a preset for a type that is not supported by the configured factory has no effect.
 */
public inline fun <reified T : Any> MockFactoryConfigurer.preset(
    noinline block: MokkeryMockConfigurer.Block<T>
) {
    preset(typeOf<T>(), block.unsafeCast())
}

/**
 * Registers a configuration [block] that is applied to every spy of type [T] created by the configured [SpyFactory].
 *
 * The [block] is applied separately to each created spy, before the block given to [SpyFactory.createOrNull],
 * so the latter takes precedence.
 *
 * Presets are matched by the exact type - a preset registered for a supertype of a created spy is not applied.
 * Type arguments are matched by their classes and nullability is ignored. A star projection matches any type argument.
 *
 * Presets are cumulative - registering a preset for a type that already has one does not replace it and all
 * matching presets are applied. They are applied from the least to the most specific match, in the registration
 * order, with presets inherited from a copied factory first, so the most specific and the most recently registered
 * preset takes precedence.
 *
 * Registering a preset for a type that is not supported by the configured factory has no effect.
 */
public inline fun <reified T : Any> SpyFactoryConfigurer.preset(noinline block: MokkerySpyConfigurer.Block<T>) {
    preset(typeOf<T>(), block.unsafeCast())
}

/**
 * Registers a configuration [block] that is applied to every mock of given [type] created
 * by the configured [MockFactory]. The [type] **must** be a class type.
 *
 * The [block] is applied separately to each created mock, before the block given to [MockFactory.createOrNull],
 * so the latter takes precedence.
 *
 * Matching and ordering rules are the same as for the [preset] overload with a generic type.
 *
 * Registering a preset for a type that is not supported by the configured factory has no effect.
 *
 * Requires casting and does not provide compile time safety. Use [preset] overload with generic type.
 *
 * @see [preset]
 */
@DelicateMokkeryApi
public fun MockFactoryConfigurer.preset(
    type: KType,
    block: MokkeryMockConfigurer.Block<Any>
) {
    presets.add(type.toMType(), block.unsafeCast())
}

/**
 * Registers a configuration [block] that is applied to every spy of given [type] created
 * by the configured [SpyFactory]. The [type] **must** be a class type.
 *
 * The [block] is applied separately to each created spy, before the block given to [SpyFactory.createOrNull],
 * so the latter takes precedence.
 *
 * Matching and ordering rules are the same as for the [preset] overload with a generic type.
 *
 * Registering a preset for a type that is not supported by the configured factory has no effect.
 *
 * Requires casting and does not provide compile time safety. Use [preset] overload with generic type.
 *
 * @see [preset]
 */
@DelicateMokkeryApi
public fun SpyFactoryConfigurer.preset(type: KType, block: MokkerySpyConfigurer.Block<Any>) {
    presets.add(type.toMType(), block.unsafeCast())
}
