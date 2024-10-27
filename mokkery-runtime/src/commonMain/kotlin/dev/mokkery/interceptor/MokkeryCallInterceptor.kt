package dev.mokkery.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.interceptor.MokkeryCallHooks

/**
 * It's invoked on each mocked function call.
 */
@DelicateMokkeryApi
public interface MokkeryCallInterceptor {

    public fun intercept(scope: MokkeryBlockingCallScope): Any?

    public suspend fun intercept(scope: MokkerySuspendCallScope): Any?

    public companion object {

        /**
         * Allows registering interceptors after a call is traced but before an answer is provided.
         */
        public val beforeAnswering: MokkeryHook<MokkeryCallInterceptor> = MokkeryCallHooks.beforeAnswering
    }
}

/**
 * Provides a set of operations available
 * in a [MokkeryCallInterceptor.intercept] and [MokkeryCallInterceptor.intercept].
 */
public interface MokkeryCallScope {

    public val context: MokkeryContext
}

/**
 * Provides a set of operations specific for [MokkeryCallInterceptor.intercept]
 */
public interface MokkeryBlockingCallScope : MokkeryCallScope

/**
 * Provides a set of operations specific for [MokkeryCallInterceptor.intercept]
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
