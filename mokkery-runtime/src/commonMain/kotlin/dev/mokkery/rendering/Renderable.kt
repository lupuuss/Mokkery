package dev.mokkery.rendering

/**
 * Implemented by objects that can produce a human-readable representation of themselves within
 * a [MokkeryRenderingScope]. Prefer this over overriding [Any.toString] when the rendering may
 * depend on configuration or tools available in the scope (e.g. value description).
 */
public interface Renderable {

    /**
     * Returns human-readable representation of this object.
     */
    context(scope: MokkeryRenderingScope)
    public fun render(): String
}

context(scope: MokkeryRenderingScope)
internal fun Any.renderOrToString(): String = when (this) {
    is Renderable -> this.render()
    else -> this.toString()
}
