package dev.mokkery.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.interceptor.MokkeryCallHooks

/**
 * It's invoked on each mocked function call.
 */
public interface MokkeryCallInterceptor {

    /**
     * Invoked on each regular mock call. To continue processing, call [MokkeryBlockingCallScope.nextIntercept].
     * The behavior following this call depends on the hook used.
     *
     * For information on available hooks and their effects, refer to [MokkeryCallInterceptor.Companion].
     */
    @DelicateMokkeryApi
    public fun intercept(scope: MokkeryBlockingCallScope): Any?

    /**
     * Invoked on each suspend mock call. To continue processing, call [MokkerySuspendCallScope.nextIntercept].
     * The behavior following this call depends on the hook used.
     *
     * For information on available hooks and their effects, refer to [MokkeryCallInterceptor.Companion].
     */
    @DelicateMokkeryApi
    public suspend fun intercept(scope: MokkerySuspendCallScope): Any?

    public companion object {

        /**
         * Allows registering interceptors after a call is traced but before an answer is provided.
         * [nextIntercept] returns value from defined answers of a fallback depending on a mock mode.
         */
        public val beforeAnswering: MokkeryHook<MokkeryCallInterceptor> = MokkeryCallHooks.beforeAnswering
    }
}

/**
 * Provides a set of operations available in a [MokkeryCallInterceptor.intercept] for both regular and suspend functions.
 */
public interface MokkeryCallScope {

    public val context: MokkeryContext
}

/**
 * Provides a set of operations available in a [MokkeryCallInterceptor.intercept] for regular functions only.
 */
public interface MokkeryBlockingCallScope : MokkeryCallScope

/**
 * Provides a set of operations available in a [MokkeryCallInterceptor.intercept] for suspend functions only.
 */
public interface MokkerySuspendCallScope : MokkeryCallScope

internal fun MokkeryBlockingCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkeryBlockingCallScope {

    return object : MokkeryBlockingCallScope {
        override val context = context

        override fun toString(): String = "MokkeryBlockingCallScope($context)"
    }
}

internal fun MokkerySuspendCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkerySuspendCallScope {

    return object : MokkerySuspendCallScope {
        override val context = context

        override fun toString(): String = "MokkerySuspendCallScope($context)"
    }
}

internal fun MokkeryBlockingCallScope.withContext(
    with: MokkeryContext = MokkeryContext.Empty
): MokkeryBlockingCallScope {
    return when {
        with === MokkeryContext.Empty -> this
        else -> MokkeryBlockingCallScope(this.context + with)
    }
}

internal fun MokkerySuspendCallScope.withContext(
    with: MokkeryContext = MokkeryContext.Empty
): MokkerySuspendCallScope {
    return when {
        with === MokkeryContext.Empty -> this
        else -> MokkerySuspendCallScope(this.context + with)
    }
}
