@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.answering.autofill

import dev.drewhamilton.poko.Poko
import dev.mokkery.internal.answering.autofill.AnyValueProvider
import dev.mokkery.internal.answering.autofill.CombinedProviders
import dev.mokkery.internal.answering.autofill.GenericArrayProvider
import dev.mokkery.internal.answering.autofill.NothingValueProvider
import dev.mokkery.internal.answering.autofill.TypeToValueAutofillProvider
import dev.mokkery.internal.answering.autofill.buildInTypesMapping
import dev.mokkery.internal.answering.autofill.compositeAutofillProvider
import kotlin.reflect.KClass

/**
 * Provides a value whenever there is a need to return a *placeholder* of certain type.
 *
 * The most important [AutofillProvider] objects are:
 * * [forInternals] - used in internal machinery.
 * * [forMockMode] - used in mock mode. Initially it only fallbacks to [forInternals]
 *
 * Both allows registering custom providers.
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
        @Poko
        public class Provided<out T>(public val value: T) : Value<T>

        /**
         * Indicates that value could not be provided.
         */
        public data object Absent : Value<Nothing>

        public companion object {

            /**
             * Returns an instance of [Provided] if [value] is not null. Otherwise, it returns [Absent].
             */
            public inline fun <T : Any> providedIfNotNull(value: () -> T?): Value<T> = value()
                ?.let(::Provided)
                ?: Absent
        }
    }

    public companion object {

        private val builtIn = CombinedProviders(
            TypeToValueAutofillProvider(buildInTypesMapping),
            NothingValueProvider,
            GenericArrayProvider,
            AnyValueProvider
        )

        /**
         * Provides default values for internal components.
         *
         * Refer to [CompositeAutofillProvider] to read about customization possibilities.
         *
         * By default, it provides:
         * * For any [Number] - 0
         * * For [Boolean] - false
         * * For [String] - ""
         * * For [KClass] - Any::class
         * * For [Unit] - [Unit]
         * * For [Result] - [Result.failure] with [IllegalStateException]
         * * For [kotlin.time.Duration] - [kotlin.time.Duration.ZERO]
         * * For arrays - array with single element according to previous rules
         * * For complex types:
         *    * Wasm - null
         *    * JS - empty object `{}`
         *    * Native - reference to `object UnsafeValue`
         *    * JVM:
         *       * value classes - instance created with reflection
         *       * other types - null
         */
        public val forInternals: CompositeAutofillProvider = compositeAutofillProvider(builtIn)

        /**
         * It is used in [dev.mokkery.MockMode.autofill] mode
         * (more specifically in [dev.mokkery.answering.Answer.Autofill]).
         * Initially it only fallbacks to [forInternals] so it provides values from it.
         * Refer to [CompositeAutofillProvider] to read about customization possibilities.
         */
        public val forMockMode: CompositeAutofillProvider = compositeAutofillProvider(forInternals)

        /**
         * Creates [AutofillProvider] that returns result of [block] as [Value.Provided] if it is not null.
         * Otherwise, returns [Value.Absent].
         */
        public inline fun <T> ofNotNull(crossinline block: (KClass<*>) -> T?): AutofillProvider<T> = AutofillProvider {
            Value.providedIfNotNull { block(it) }
        }
    }
}

/**
 * Calls [AutofillProvider.provide] and returns [AutofillProvider.Value.Provided.value] if present.
 */
public inline fun <T> AutofillProvider<T>.provideValue(type: KClass<*>): T? = provide(type).getIfProvided()

/**
 * Returns value if [this] is an instance of [AutofillProvider.Value.Provided].
 */
public fun <T> AutofillProvider.Value<T>.getIfProvided(): T? = (this as? AutofillProvider.Value.Provided<T>)?.value

/**
 * Returns true if [this] is an instance of [AutofillProvider.Value.Provided]
 */
public fun AutofillProvider.Value<*>.isProvided(): Boolean = this != AutofillProvider.Value.Absent
