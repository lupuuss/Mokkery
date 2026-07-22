package dev.mokkery.interceptor

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.callInterceptor
import dev.mokkery.internal.withContext

/**
 * It's invoked on each mocked function call.
 */
public interface MokkeryCallInterceptor {

    /**
     * Invoked on each regular mock call. To continue processing, call [nextIntercept].
     * The behavior following this call depends on the hook used.
     *
     * For information on available hooks and their effects, refer to [MokkeryCallInterceptor.Companion].
     */
    @DelicateMokkeryApi
    public fun intercept(scope: MokkeryBlockingCallScope): Any?

    /**
     * Invoked on each suspend mock call. To continue processing, call [nextIntercept].
     * The behavior following this call depends on the hook used.
     *
     * For information on available hooks and their effects, refer to [MokkeryCallInterceptor.Companion].
     */
    @DelicateMokkeryApi
    public suspend fun intercept(scope: MokkerySuspendCallScope): Any?

    public companion object {

        /**
         * Allows registering interceptors after a call is traced but before an answer is provided.
         * [nextIntercept] returns value from defined answers or a fallback depending on a mock mode.
         */
        @Deprecated(
            message = "Deprecated in favor of `callHooks` property that allows accessing separate hooks for different scopes." +
                    " Read more in `MokkeryCallHooks` documentation.",
            replaceWith = ReplaceWith(
                expression = "MokkeryScope.global.callHooks.beforeAnswering",
                imports = arrayOf("dev.mokkery.MokkeryScope", "dev.mokkery.interceptor.callHooks")
            )
        )
        public val beforeAnswering: MokkeryHook<MokkeryCallInterceptor> get() = MokkeryScope.global
            .callHooks
            .beforeAnswering
    }
}

/**
 * Calls [dev.mokkery.interceptor.MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public fun MokkeryBlockingCallScope.nextIntercept(
    context: MokkeryContext = MokkeryContext.Empty
): Any? = callInterceptor.intercept(withContext(context))

/**
 * Calls [dev.mokkery.interceptor.MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public suspend fun MokkerySuspendCallScope.nextIntercept(
    context: MokkeryContext = MokkeryContext.Empty
): Any? = callInterceptor.intercept(withContext(context))

