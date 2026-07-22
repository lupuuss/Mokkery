package dev.mokkery.interceptor

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require

/**
 * Returns [MokkeryCallHooks] associated with this scope. See [MokkeryCallHooks] for details on hook scoping.
 */
public val MokkeryScope.callHooks: MokkeryCallHooks
    get() = mokkeryContext.require(MokkeryCallHooks)

/**
 * Provides access to hooks that allow registering custom [MokkeryCallInterceptor]s in the call pipeline.
 *
 * Hooks are arbitrarily scoped. Currently, there are 3 scopes to register call interceptors:
 * * Global - [MokkeryScope.global]
 * * Suite-level [dev.mokkery.MokkerySuiteScope]
 * * Instance-level [MokkeryScope.from]
 *
 * Interceptors registered in a broader scope (e.g. global one) affect narrower scopes (e.g. instance-level one).
 */
public interface MokkeryCallHooks : MokkeryContext.Element {

    override val key: Key get() = Key

    /**
     * Allows registering interceptors before a call is traced.
     * [nextIntercept] traces current call.
     */
    public val beforeTracing: MokkeryHook<MokkeryCallInterceptor>

    /**
     * Allows registering interceptors after a call is traced but before an answer is provided.
     * [nextIntercept] returns value from defined answers or a fallback depending on a mock mode.
     */
    public val beforeAnswering: MokkeryHook<MokkeryCallInterceptor>

    public companion object Key : MokkeryContext.Key<MokkeryCallHooks>
}
