package dev.mokkery.internal.verify.render

import dev.mokkery.rendering.Renderer
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.rendering.callTraceRenderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.rendering.MokkeryRenderingScope

internal object TemplateMatchingResultsRenderer : Renderer<List<TemplateMatchingResult>> {

    override val key get() = VerifyRendering.templateMatchingResults

    context(scope: MokkeryRenderingScope)
    override fun render(value: List<TemplateMatchingResult>): String = buildString {
        var templateCounter = 1
        val indexingColumnSize = value.size.toString().length + 2
        appendLine("Expected calls with matches (x.) and unverified calls (*) in order:")
        value.forEach {
            when (it) {
                is TemplateMatchingResult.Matching -> {
                    appendTemplateLines(it.template, it.trace, templateCounter++, indexingColumnSize)
                }
                is TemplateMatchingResult.NoMatch -> {
                    appendTemplateLines(it.template, null, templateCounter++, indexingColumnSize)
                }
                is TemplateMatchingResult.UnverifiedCall -> appendUnverifiedCallLine(it, indexingColumnSize)
            }
        }
    }

    context(scope: MokkeryRenderingScope)
    private fun StringBuilder.appendUnverifiedCallLine(
        call: TemplateMatchingResult.UnverifiedCall,
        columnSize: Int
    ) {
        append("*".padEnd(columnSize, ' '))
        append("  ")
        appendLine(scope.callTraceRenderer.render(call.trace))
    }

    context(scope: MokkeryRenderingScope)
    private fun StringBuilder.appendTemplateLines(
        template: CallTemplate,
        trace: CallTrace?,
        index: Int,
        columnSize: Int
    ) {
        append("$index. ".padEnd(columnSize))
        append("┌ ")
        appendLine(scope.callTemplateRenderer.render(template))
        append(" ".padEnd(columnSize))
        append("└ ")
        if (trace != null) {
            appendLine(scope.callTraceRenderer.render(trace))
        } else {
            appendLine("No matching call!")
        }
    }
}
