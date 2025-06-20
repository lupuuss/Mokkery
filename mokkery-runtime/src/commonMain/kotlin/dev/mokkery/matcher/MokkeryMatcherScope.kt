package dev.mokkery.matcher

import dev.mokkery.MokkeryScope

/**
 * Scope for declaring argument matchers.
 */
public interface MokkeryMatcherScope : MokkeryScope

/**
 * Scope for declaring argument matchers.
 *
 * **DEPRECATED: It was renamed to `MokkeryMatcherScope`**
 */
@Deprecated(
    "Renamed to MokkeryMatcherScope",
    replaceWith = ReplaceWith("MokkeryMatcherScope", "dev.mokkery.matcher.MokkeryMatcherScope"),
    level = DeprecationLevel.ERROR
)
public typealias ArgMatchersScope = MokkeryMatcherScope
