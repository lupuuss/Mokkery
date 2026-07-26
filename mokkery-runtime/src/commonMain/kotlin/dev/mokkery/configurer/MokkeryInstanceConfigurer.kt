package dev.mokkery.configurer

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkeryMockScope
import dev.mokkery.MokkerySpyScope

/**
 * Returns the [MokkeryInstanceConfigurer] of the mock/spy being configured. It is available in mock/spy blocks.
 *
 * @throws dev.mokkery.MokkeryRuntimeException if this instance is not the one being configured.
 */
context(aware: MokkeryInstanceConfigurer.Aware<T, C>)
public val <T : Any, C : MokkeryInstanceConfigurer> T.configurer: C
    get() = aware.configurer(this)

/**
 * [MokkeryConfigurer] for [MokkeryInstanceScope].
 *
 * This configurer is not used directly as context parameter with mock/spy blocks. [Aware] is used instead.
 * It's caused by the fact that a declaration with this configurer as a context parameter loses resolution to
 * a declaration of the same name available on an outer receiver (e.g. an enclosing [dev.mokkery.MokkerySuiteScope]).
 * With [Aware], such declarations are extensions on the configured instance, so they resolve correctly.
 */
public interface MokkeryInstanceConfigurer : MokkeryConfigurer, MokkeryInstanceScope {

    /**
     * Provides the [MokkeryInstanceConfigurer] of type [C] for the configured instance of type [T].
     */
    public interface Aware<in T : Any, out C : MokkeryInstanceConfigurer> {

        /**
         * Returns the configurer for [value].
         *
         * @throws dev.mokkery.MokkeryRuntimeException if [value] is not the instance being configured.
         */
        public fun configurer(value: T): C
    }

    /**
     * Configuration block for an instance of type [T] with a configurer of type [C].
     */
    public typealias Block<T, C> = context(Aware<T, C>)T.() -> Unit
}

/**
 * [MokkeryInstanceConfigurer] for [MokkeryMockScope].
 *
 * @see [MokkeryInstanceConfigurer]
 */
public interface MokkeryMockConfigurer : MokkeryInstanceConfigurer, MokkeryMockScope {

    /**
     * [MokkeryInstanceConfigurer.Aware] that provides [MokkeryMockConfigurer] for a mock of type [T].
     */
    public typealias Aware<T> = MokkeryInstanceConfigurer.Aware<T, MokkeryMockConfigurer>

    /**
     * Configuration block for a mock of type [T].
     */
    public typealias Block<T> = MokkeryInstanceConfigurer.Block<T, MokkeryMockConfigurer>
}

/**
 * [MokkeryInstanceConfigurer] for [MokkerySpyScope].
 *
 * @see [MokkeryInstanceConfigurer]
 */
public interface MokkerySpyConfigurer : MokkeryInstanceConfigurer, MokkerySpyScope {

    /**
     * [MokkeryInstanceConfigurer.Aware] that provides [MokkerySpyConfigurer] for a spy of type [T].
     */
    public typealias Aware<T> = MokkeryInstanceConfigurer.Aware<T, MokkerySpyConfigurer>

    /**
     * Configuration block for a spy of type [T].
     */
    public typealias Block<T> = MokkeryInstanceConfigurer.Block<T, MokkerySpyConfigurer>
}
