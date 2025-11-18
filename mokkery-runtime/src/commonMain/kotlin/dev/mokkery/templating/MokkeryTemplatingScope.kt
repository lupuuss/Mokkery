package dev.mokkery.templating

import dev.mokkery.matcher.MokkeryMatcherScope

/**
 * Scope for declaring call templates. It's used with [dev.mokkery.every] and [dev.mokkery.verify].
 * Currently, it's illegal to declare functions that accept this scope.
 *
 * Example of a call template inside `verify`:
 * ```kotlin
 * verify {
 *   mock.call(any())
 * }
 * ```
 */
public interface MokkeryTemplatingScope : MokkeryMatcherScope
