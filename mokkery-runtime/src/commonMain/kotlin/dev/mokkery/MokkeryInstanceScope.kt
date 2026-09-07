package dev.mokkery

/**
 * Provides a set of operations available in a Mokkery mock/spy context.
 */
public interface MokkeryInstanceScope : MokkeryScope

/**
 * Provides a set of operations available in a Mokkery mock context.
 */
public interface MokkeryMockScope : MokkeryInstanceScope

/**
 * Provides a set of operations available in a Mokkery spy context.
 */
public interface MokkerySpyScope : MokkeryInstanceScope
