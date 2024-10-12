package dev.mokkery.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.interceptor.nextInterceptor

/**
 * It's invoked on each mocked function call.
 */
@DelicateMokkeryApi
public interface MokkeryCallInterceptor {

    public fun intercept(scope: MokkeryCallScope): Any?

    public suspend fun interceptSuspend(scope: MokkeryCallScope): Any?
}

/**
 * Provides a set of operations available
 * in a [MokkeryCallInterceptor.intercept] and [MokkeryCallInterceptor.interceptSuspend].
 */
public interface MokkeryCallScope {

    public val context: MokkeryContext
}

/**
 * Calls [MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public fun MokkeryCallScope.nextIntercept(context: MokkeryContext = MokkeryContext.Empty): Any? {
    return this.context.nextInterceptor.intercept(withContext(context))
}

/**
 * Calls [MokkeryCallInterceptor.interceptSuspend] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public suspend fun MokkeryCallScope.nextInterceptSuspend(context: MokkeryContext = MokkeryContext.Empty): Any? {
    return this.context.nextInterceptor.interceptSuspend(withContext(context))
}

internal fun MokkeryCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkeryCallScope {
    return object : MokkeryCallScope {
        override val context = context

        override fun toString(): String = "MokkeryCallScope($context)"
    }
}

internal fun MokkeryCallScope.withContext(with: MokkeryContext = MokkeryContext.Empty): MokkeryCallScope {
    return when {
        with === MokkeryContext.Empty -> this
        else -> MokkeryCallScope(this.context + with)
    }
}
