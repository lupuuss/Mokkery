package dev.mokkery.interceptor

import dev.mokkery.MokkeryScope

/**
 * Provides a set of operations available in a [MokkeryCallInterceptor.intercept] for both regular and suspend functions.
 */
public interface MokkeryCallScope : MokkeryScope

/**
 * Provides a set of operations available in a [MokkeryCallInterceptor.intercept] for regular functions only.
 */
public interface MokkeryBlockingCallScope : MokkeryCallScope

/**
 * Provides a set of operations available in a [MokkeryCallInterceptor.intercept] for suspend functions only.
 */
public interface MokkerySuspendCallScope : MokkeryCallScope

