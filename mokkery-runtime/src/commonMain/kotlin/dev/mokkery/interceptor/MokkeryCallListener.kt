package dev.mokkery.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi

/**
 * Simplifies creation of [MokkeryCallInterceptor] that does not need to
 * change the current pipeline and only gets notified on each call.
 */
public interface MokkeryCallListener : MokkeryCallInterceptor {

    public fun onIntercept(scope: MokkeryCallScope)

    /**
     * **Do not override this method to keep [MokkeryCallListener] behaviour as intended.**
     *
     * Calls [onIntercept] and then [MokkeryBlockingCallScope.nextIntercept]
     */
    @DelicateMokkeryApi
    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        onIntercept(scope)
        return scope.nextIntercept()
    }

    /**
     * **Do not override this method to keep [MokkeryCallListener] behaviour as intended.**
     *
     * Calls [onIntercept] and then [MokkerySuspendCallScope.nextIntercept]
     */
    @DelicateMokkeryApi
    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        onIntercept(scope)
        return scope.nextIntercept()
    }
}

/**
 * Creates [MokkeryCallListener] that invokes [block] on each call.
 */
public fun MokkeryCallListener(block: (MokkeryCallScope) -> Unit): MokkeryCallListener {
    return object : MokkeryCallListener {
        override fun onIntercept(scope: MokkeryCallScope) = block(scope)
    }
}
