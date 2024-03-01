package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer.Suspending
import dev.mokkery.internal.NoMoreSequentialAnswersException
import dev.mokkery.internal.SuspendingFunctionBlockingCallException
import dev.mokkery.internal.answering.BlockingCallDefinitionScope
import dev.mokkery.internal.answering.SuspendCallDefinitionScope
import dev.mokkery.internal.answering.autofillValue
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

/**
 * An answer for a function call. For blocking answers only [call] implementation is required. For suspending answers
 * [callSuspend] should be implemented and [call] implementation should throw an exception. Use [Suspending] for convenience.
 *
 */
@DelicateMokkeryApi
public interface Answer<out T> {

    /**
     * Provides a return value for a function call with given [scope].
     */
    public fun call(scope: FunctionScope): T

    /**
     * Just like [call] but suspends. By default, it calls [call].
     */
    public suspend fun callSuspend(scope: FunctionScope): T = call(scope)

    /**
     * Convenience interface for custom suspending answers. By default, it throws runtime exception on [call].
     */
    public interface Suspending<T> : Answer<T> {

        /**
         * By default, it throws runtime exception.
         */
        override fun call(scope: FunctionScope): Nothing = throw SuspendingFunctionBlockingCallException()
    }

    /**
     * Returns [value] on [call] and [callSuspend].
     */
    public data class Const<T>(val value: T) : Answer<T> {
        override fun call(scope: FunctionScope): T = value
    }

    /**
     * Calls [block] on [call] and [callSuspend].
     */
    public data class Block<T>(val block: BlockingCallDefinitionScope<T>.(CallArgs) -> T) : Answer<T> {
        override fun call(scope: FunctionScope): T = block(BlockingCallDefinitionScope(scope), CallArgs(scope.args))
    }

    /**
     * Throws [throwable] on [call] and [callSuspend]
     */
    public data class Throws(val throwable: Throwable) : Answer<Nothing> {
        override fun call(scope: FunctionScope): Nothing = throw throwable
    }

    /**
     * Just like [Block] but for suspending functions.
     */
    public data class BlockSuspend<T>(val block: suspend SuspendCallDefinitionScope<T>.(CallArgs) -> T) : Suspending<T> {

        override suspend fun callSuspend(scope: FunctionScope): T {
            return block(SuspendCallDefinitionScope(scope), CallArgs(scope.args))
        }
    }

    /**
     * Provides *empty* value for standard types (e.g. 0 for numbers, "" for string, null for complex type).
     */
    public object Autofill : Answer<Any?> {
        override fun call(scope: FunctionScope): Any? = autofillValue(scope.returnType)
    }

    /**
     * Interface for every answer that have to be called in repeat when specified in [sequentially].
     */
    public interface Sequential<T> : Answer<T> {

        /**
         * Returns true if answer should be called again.
         */
        public fun hasNext(): Boolean
    }

    /**
     * Returns results of answers from [iterator] until empty. It supports nested [Sequential] answers and calls
     * them until they are empty.
     */
    public data class SequentialByIterator<T>(val iterator: Iterator<Answer<T>>) : Sequential<T> {

        private val lock = reentrantLock()
        private var nestedSequential: Sequential<T>? by atomic(null)

        override fun hasNext(): Boolean = lock.withLock { iterator.hasNext() || (nestedSequential?.hasNext() ?: false) }

        override fun call(scope: FunctionScope): T = getCurrent().call(scope)

        override suspend fun callSuspend(scope: FunctionScope): T = getCurrent().callSuspend(scope)

        private fun getCurrent(): Answer<T> = lock.withLock {
            val nested = nestedSequential
            if (nested != null) {
                if (nested.hasNext()) return nested
                nestedSequential = null
            }
            if (!iterator.hasNext()) throw NoMoreSequentialAnswersException()
            val next = iterator.next()
            if (next is Sequential<T>) nestedSequential = next
            return next
        }
    }
}
