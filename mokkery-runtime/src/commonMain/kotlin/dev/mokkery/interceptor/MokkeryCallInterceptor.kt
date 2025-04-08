package dev.mokkery.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
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
