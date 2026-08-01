package dev.mokkery.rendering

import dev.mokkery.MokkeryCallScope
import dev.mokkery.context.require
import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.matcher.ArgMatcher

/**
 * Renders given [MokkeryCallScope] as human-readable string.
 */
public val MokkeryRenderingScope.callScopeRenderer: Renderer<MokkeryCallScope>
    get() = mokkeryContext.require(MokkeryRendering.callScopeKey)

/**
 * Renders common types (e.g. [String], [Array]) in more readable way.
 */
public val MokkeryRenderingScope.descriptionRenderer: Renderer<Any?>
    get() = mokkeryContext.require(MokkeryRendering.descriptionKey)

/**
 * Calls [Renderable.render] on given [ArgMatcher] if it's implemented or calls [Any.toString].
 */
public val MokkeryRenderingScope.argMatcherRenderer: Renderer<ArgMatcher<*>>
    get() = mokkeryContext.require(MokkeryRendering.argMatcherKey)
