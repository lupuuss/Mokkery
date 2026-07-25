package dev.mokkery.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.call
import dev.mokkery.callOriginal
import dev.mokkery.callSuper
import dev.mokkery.context.argValue
import dev.mokkery.context.argValues
import dev.mokkery.internal.BlockingAnswerSuspendingCallException
import dev.mokkery.internal.NoMoreSequentialAnswersException
import dev.mokkery.internal.SuspendingAnswerBlockingCallException
import dev.mokkery.internal.rendering.descriptionRenderer
import dev.mokkery.internal.rendering.renderingScope
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import dev.mokkery.self
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

/**
 * An answer for a function call.
 *
 * * [call] with [MokkeryBlockingCallScope] is invoked on blocking function call.
 * * [call] with [MokkerySuspendCallScope] is invoked on suspending function call.
 *
 * Tips for providing correct implementation:
 * * If you want to provide the same implementation for both blocking and suspending call, implement [Answer.Unified] instead.
 * * If you want your answer to support only blocking or suspending calls, implement [Answer.Blocking] or [Answer.Suspending].
 * * Implement pure [Answer] if you want implement both methods separately.
 * * Answers should not be used directly. It's a good practice to provide extension based API.
 *
 * Check existing answers implementations for samples.
 */
@DelicateMokkeryApi
public interface Answer<out T> {

    /**
     * Provides a return value for a blocking function call with given [scope].
     */
    public fun call(scope: MokkeryBlockingCallScope): T

    /**
     * Provides a return value for a suspend function call with given [scope].
     */
    public suspend fun call(scope: MokkerySuspendCallScope): T

    /**
     * Returns human-readable answer description. By default, it returns `answers $this`.
     * It's used for debugging purposes.
     */
    @Deprecated("Implement `dev.mokkery.rendering.Renderable` interface instead.",)
    public fun description(): String = when (this) {
        is Renderable -> context(MokkeryScope.global.renderingScope) { this.render() }
        else -> "answers $this"
    }

    /**
     * Convenience interface for blocking only answers. By default, it throws runtime exception on suspending [call].
     */
    public interface Blocking<T> : Answer<T> {

        /**
         * By default, it throws runtime exception.
         */
        override suspend fun call(scope: MokkerySuspendCallScope): Nothing = throw BlockingAnswerSuspendingCallException()
    }

    /**
     * Convenience interface for suspend only answers. By default, it throws runtime exception on blocking [call].
     */
    public interface Suspending<T> : Answer<T> {

        /**
         * By default, it throws runtime exception.
         */
        override fun call(scope: MokkeryBlockingCallScope): Nothing = throw SuspendingAnswerBlockingCallException()
    }

    /**
     * Convenience interface for answers with identical implementation for both blocking and suspending call.
     */
    public interface Unified<T> : Answer<T> {

        public fun call(scope: MokkeryCallScope): T

        override fun call(scope: MokkeryBlockingCallScope): T = call(scope as MokkeryCallScope)

        override suspend fun call(scope: MokkerySuspendCallScope): T = call(scope as MokkeryCallScope)
    }

    /**
     * Returns [value] on [call].
     */
    @Poko
    public class Const<T>(public val value: T) : Unified<T>, Renderable {

        override fun call(scope: MokkeryCallScope): T = value

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "returns ${descriptionRenderer.render(value)}"
    }

    /**
     * Calls [block] on [call].
     */
    @Poko
    public class Block<T>(public val block: BlockingCallDefinitionScope<T>.(CallArgs) -> T) : Blocking<T>, Renderable {

        override fun call(scope: MokkeryBlockingCallScope): T = block(
            BlockingCallDefinitionScope(scope),
            CallArgs(scope.call.argValues)
        )

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "calls {...}"
    }

    /**
     * Throws [throwable] on [call].
     */
    @Poko
    public class Throws(public val throwable: Throwable) : Unified<Nothing>, Renderable {

        override fun call(scope: MokkeryCallScope): Nothing = throw throwable

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "throws $throwable"
    }

    /**
     * Just like [Block] but for suspending functions.
     */
    @Poko
    public class BlockSuspend<T>(public val block: suspend SuspendCallDefinitionScope<T>.(CallArgs) -> T) : Suspending<T>, Renderable {

        override suspend fun call(scope: MokkerySuspendCallScope): T = block(
            SuspendCallDefinitionScope(scope),
            CallArgs(scope.call.argValues)
        )

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "calls {...}"
    }

    /**
     * Used whenever there is no defined answer for a call to mock that is in [dev.mokkery.MockMode.autofill].
     * Refer to [AutofillProvider.forMockMode] to read more about returned values.
     */
    public object Autofill : Unified<Any?> {

        override fun call(scope: MokkeryCallScope): Any? = AutofillProvider
            .forMockMode
            .provideValue(scope.call.function.returnType)
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
    @Poko
    public class SequentialByIterator<T>(public val iterator: Iterator<Answer<T>>) : Sequential<T>, Renderable {

        private val lock = reentrantLock()
        private var nestedSequential: Sequential<T>? by atomic(null)

        override fun hasNext(): Boolean = lock.withLock { iterator.hasNext() || (nestedSequential?.hasNext() == true) }

        override fun call(scope: MokkeryBlockingCallScope): T = getCurrent().call(scope)

        override suspend fun call(scope: MokkerySuspendCallScope): T = getCurrent().call(scope)

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "sequentially {...}"

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
