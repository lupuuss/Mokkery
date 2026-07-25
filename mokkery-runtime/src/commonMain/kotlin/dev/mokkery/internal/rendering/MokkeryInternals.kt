package dev.mokkery.internal.rendering

import dev.mokkery.MokkeryCallScope
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.MokkeryInternals
import dev.mokkery.internal.renderingScope
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.rendering.MokkeryRenderingScope

@InternalMokkeryApi
public val MokkeryInternals<MokkeryRenderingScope>.argMatcherRenderer: Renderer<ArgMatcher<*>>
    get() = renderingScope.argMatcherRenderer

@InternalMokkeryApi
public val MokkeryInternals<MokkeryRenderingScope>.descriptionRenderer: Renderer<Any?>
    get() = renderingScope.descriptionRenderer

@InternalMokkeryApi
public val MokkeryInternals<MokkeryRenderingScope>.callScopeRenderer: Renderer<MokkeryCallScope>
    get() = renderingScope.callScopeRenderer
