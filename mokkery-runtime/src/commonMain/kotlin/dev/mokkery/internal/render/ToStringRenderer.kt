package dev.mokkery.internal.render

internal object ToStringRenderer : Renderer<Any?> {

    override fun render(value: Any?): String = value.toString()
}
