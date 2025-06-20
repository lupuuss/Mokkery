package dev.mokkery.templating

import dev.mokkery.matcher.ArgMatchersScope

/**
 * Scope for marking blocks passed to [dev.mokkery.every] and [dev.mokkery.verify].
 * Currently, it's illegal to declare functions that accept this scope.
 */
public interface TemplatingScope : ArgMatchersScope
