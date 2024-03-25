package dev.mokkery.internal.verify.render

import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.ToStringRenderer
import dev.mokkery.internal.tracing.CallTrace

internal class CallTraceListRenderer(
    private val point: String = "*",
    private val traceRenderer: Renderer<CallTrace> = ToStringRenderer
) : Renderer<List<CallTrace>> {
    override fun render(value: List<CallTrace>): String = buildString {
        value.forEach {
            append(point)
            append(" ")
            appendLine(traceRenderer.render(it))
        }
    }
}
