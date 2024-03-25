package dev.mokkery.internal.render

internal fun interface Renderer<in T> {

    fun render(value: T): String
}
