package dev.mokkery.internal.render

/**
 * Renders building tends to be expensive - especially for [dev.mokkery.internal.verify.Verifier]s.
 * This class allows to postpone [Renderer] creation until it's required.
 */
internal fun interface RendererFactory<in T> {

    fun create(): Renderer<T>
}
