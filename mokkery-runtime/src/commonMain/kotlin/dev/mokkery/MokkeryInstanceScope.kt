package dev.mokkery

/**
 * Provides a set of operations available in a Mokkery mock/spy context.
 *
 * Currently, it does not provide any operations, and it's exposed for internal use and potentially for public use in the future.
 */
public interface MokkeryInstanceScope : MokkeryScope

/**
 * Provides a set of operations available in a Mokkery mock context.
 *
 * Currently, it does not provide any operations, and it's exposed for internal use and potentially for public use in the future.
 */
public interface MokkeryMockScope : MokkeryInstanceScope

/**
 * Provides a set of operations available in a Mokkery spy context.
 *
 * Currently, it does not provide any operations, and it's exposed for internal use and potentially for public use in the future.
 */
public interface MokkerySpyScope : MokkeryInstanceScope
