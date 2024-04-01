package dev.mokkery.answering.autofill

import dev.mokkery.internal.answering.autofill.AnyValueProvider
import dev.mokkery.internal.answering.autofill.CombinedProviders
import dev.mokkery.internal.answering.autofill.GenericArrayProvider
import dev.mokkery.internal.answering.autofill.NothingValueProvider
import dev.mokkery.internal.answering.autofill.ValuesMapProvider
import dev.mokkery.internal.answering.autofill.buildInTypesMapping
import kotlin.reflect.KClass

/**
 * Provides a value whenever there is a call to not mocked function in [dev.mokkery.MockMode.autofill].
 */
public fun interface AutofillProvider<out T> {

    /**
     * Returns an instance of [Value.Provided], whenever value of [type] can be provided.
     * Otherwise, it returns [Value.Absent]
     */
    public fun provide(type: KClass<*>): Value<T>

    /**
     * Result of [AutofillProvider.provide] call.
     */
    public sealed interface Value<out T> {

        /**
         * Represents provided [value].
         */
        public class Provided<out T>(public val value: T) : Value<T> {

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false
                other as Provided<*>
                return value == other.value
            }

            override fun hashCode(): Int = value?.hashCode() ?: 0

            override fun toString(): String = "Provided(value=$value)"
        }

        /**
         * Indicates that value could not be provided.
         */
        public data object Absent : Value<Nothing>

        public companion object {

            /**
             * Returns an instance of [Provided] if [value] is not null. Otherwise, it returns [Absent].
             */
            public inline fun <T : Any> providedIfNotNull(value: () -> T?) : Value<T> = value()
                ?.let(::Provided)
                ?: Absent
        }
    }

    /**
     * Implementation of [AutofillProvider] that is used by [dev.mokkery.answering.Answer.Autofill].
     * By default, it provides:
     * * For [Char] - '0'
     * * For any [Number] - 0
     * * For [Boolean] - false
     * * For [String] - ""
     * * For [KClass] - Any::class
     * * For [Unit] - [Unit]
     * * For complex types:
     *    * JVM and Wasm - null
     *    * JS - `object {}`
     *    * Native - reference to `object UnsafeValue`
     *  * For arrays - array with single element according to previous rules
     */
    public companion object : AutofillProvider<Any?> {

        internal val builtIn = CombinedProviders(
            ValuesMapProvider(buildInTypesMapping),
            NothingValueProvider,
            GenericArrayProvider,
            AnyValueProvider
        )

        override fun provide(type: KClass<*>): Value<Any?> = builtIn.provide(type)
    }
}

/**
 * Returns value if [this] is an instance of [AutofillProvider.Value.Provided].
 */
public fun <T> AutofillProvider.Value<T>.getIfProvided(): T? = (this as? AutofillProvider.Value.Provided<T>)?.value

/**
 * Returns true if [this] is an instance of [AutofillProvider.Value.Provided]
 */
public fun AutofillProvider.Value<*>.isProvided(): Boolean = this != AutofillProvider.Value.Absent

/**
 * Calls [AutofillProvider.provide] and returns [AutofillProvider.Value.Provided.value] if present.
 */
public inline fun <T> AutofillProvider<T>.provideValue(type: KClass<*>): T? = provide(type).getIfProvided()
