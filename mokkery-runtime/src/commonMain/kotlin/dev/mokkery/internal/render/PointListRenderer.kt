package dev.mokkery.internal.render

internal class PointListRenderer<T>(
    private val point: String = "*",
    private val itemRenderer: Renderer<T> = ToStringRenderer
) : Renderer<List<T>> {
    override fun render(value: List<T>): String = buildString {
        value.forEach {
            append(point)
            append(" ")
            appendLine(itemRenderer.render(it))
        }
    }
}
