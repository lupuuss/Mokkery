package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer.Suspending
import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.internal.NoMoreSequentialAnswersException
import dev.mokkery.internal.SuspendingFunctionBlockingCallException
import dev.mokkery.internal.answering.BlockingCallDefinitionScope
import dev.mokkery.internal.answering.SuspendCallDefinitionScope
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
    public class Const<T>(public val value: T) : Answer<T> {

        override fun call(scope: FunctionScope): T = value

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Const<*>
            return value == other.value
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0

        override fun toString(): String = "Const(value=$value)"
    }

    /**
     * Calls [block] on [call] and [callSuspend].
     */
    public class Block<T>(public val block: BlockingCallDefinitionScope<T>.(CallArgs) -> T) : Answer<T> {

        override fun call(scope: FunctionScope): T = block(BlockingCallDefinitionScope(scope), CallArgs(scope.args))

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Block<*>
            return block == other.block
        }

        override fun hashCode(): Int = block.hashCode()

        override fun toString(): String = "Block(block=$block)"
    }

    /**
     * Throws [throwable] on [call] and [callSuspend]
     */
    public class Throws(public val throwable: Throwable) : Answer<Nothing> {

        override fun call(scope: FunctionScope): Nothing = throw throwable

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Throws
            return throwable == other.throwable
        }

        override fun hashCode(): Int = throwable.hashCode()

        override fun toString(): String = "Throws(throwable=$throwable)"
    }

    /**
     * Just like [Block] but for suspending functions.
     */
    public class BlockSuspend<T>(public val block: suspend SuspendCallDefinitionScope<T>.(CallArgs) -> T) : Suspending<T> {

        override suspend fun callSuspend(scope: FunctionScope): T {
            return block(SuspendCallDefinitionScope(scope), CallArgs(scope.args))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as BlockSuspend<*>
            return block == other.block
        }

        override fun hashCode(): Int = block.hashCode()

        override fun toString(): String = "BlockSuspend(block=$block)"
    }

    /**
     * Used whenever there is no defined answer for a call to mock that is in [dev.mokkery.MockMode.autofill].
     * Refer to [AutofillProvider.forMockMode] to read more about returned values.
     */
    public object Autofill : Answer<Any?> {

        override fun call(scope: FunctionScope): Any? = AutofillProvider.forMockMode.provideValue(scope.returnType)
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
    public class SequentialByIterator<T>(public val iterator: Iterator<Answer<T>>) : Sequential<T> {

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

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as SequentialByIterator<*>
            return iterator == other.iterator
        }

        override fun hashCode(): Int = iterator.hashCode()

        override fun toString(): String = "SequentialByIterator(iterator=$iterator)"
    }
}
