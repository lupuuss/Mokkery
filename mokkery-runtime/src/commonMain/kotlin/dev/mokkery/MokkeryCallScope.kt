package dev.mokkery

/**
 * Provides a set of operations available in any mocked function context.
 */
public interface MokkeryCallScope : MokkeryScope

/**
 * Provides a set of operations available in non-suspendable mocked function context.
 */
public interface MokkeryBlockingCallScope : MokkeryCallScope

/**
 * Provides a set of operations available in suspendable mocked function context.
 */
public interface MokkerySuspendCallScope : MokkeryCallScope

