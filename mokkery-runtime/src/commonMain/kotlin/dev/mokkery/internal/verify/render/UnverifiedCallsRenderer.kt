package dev.mokkery.internal.verify.render

import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.tracing.CallTrace

internal class UnverifiedCallsRenderer(
    private val traceListRenderer: Renderer<List<CallTrace>> = PointListRenderer()
) : Renderer<List<CallTrace>> {
    override fun render(value: List<CallTrace>): String = buildString {
        appendLine("All expected calls have been satisfied! However, there should not be any unverified calls, yet these are present:")
        append(traceListRenderer.render(value))
    }
}
