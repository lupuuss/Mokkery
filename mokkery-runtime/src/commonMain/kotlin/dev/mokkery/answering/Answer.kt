package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.SuspendingFunctionBlockingCallException
import dev.mokkery.internal.answering.autofillValue
import kotlin.reflect.KClass

/**
 * An answer for a function call. For blocking answers only [call] implementation is required. For suspending answers
 * [callSuspend] should be implemented and [call] implementation should throw an exception. Use [Suspending] for convenience.
 *
 */
@DelicateMokkeryApi
public interface Answer<out T> {

    /**
     * Provides a return value for a function call with given [returnType] and [args].
     * If function has any extension receiver, it is provided at the beginning of the [args] list.
     */
    public fun call(returnType: KClass<*>, args: List<Any?>): T

    /**
     * Just like [call] but suspends. By default, it calls [call].
     */
    public suspend fun callSuspend(returnType: KClass<*>, args: List<Any?>): T = call(returnType, args)

    /**
     * Convenience interface for custom suspending answers. By default, it throws runtime exception on [call].
     */
    public interface Suspending<T> : Answer<T> {

        /**
         * By default, it throws runtime exception.
         */
        override fun call(returnType: KClass<*>, args: List<Any?>): Nothing = throw SuspendingFunctionBlockingCallException()
    }

    /**
     * Returns [value] on [call] and [callSuspend].
     */
    public class Const<T>(private val value: T) : Answer<T> {
        override fun call(returnType: KClass<*>, args: List<Any?>): T = value
    }

    /**
     * Calls [block] on [call] and [callSuspend].
     */
    public class Block<T>(private val block: (FunctionScope) -> T) : Answer<T> {
        override fun call(returnType: KClass<*>, args: List<Any?>): T = block(FunctionScope(returnType, args))
    }

    /**
     * Throws [throwable] on [call] and [callSuspend]
     */
    public class Throws(private val throwable: Throwable) : Answer<Nothing> {
        override fun call(returnType: KClass<*>, args: List<Any?>): Nothing = throw throwable
    }

    /**
     * Just like [Block] but for suspending functions.
     */
    public class BlockSuspend<T>(private val block: suspend (FunctionScope) -> T) : Suspending<T> {

        override suspend fun callSuspend(returnType: KClass<*>, args: List<Any?>): T {
            return block(FunctionScope(returnType, args))
        }
    }

    /**
     * Provides *empty* value for standard types (e.g. 0 for numbers, "" for string, null for complex type).
     */
    public object Autofill : Answer<Any?> {
        override fun call(returnType: KClass<*>, args: List<Any?>): Any? = autofillValue(returnType)
    }

}
