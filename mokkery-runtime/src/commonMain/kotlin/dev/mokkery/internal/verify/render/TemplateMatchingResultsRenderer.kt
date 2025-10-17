package dev.mokkery.internal.verify.render

import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.ToStringRenderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateMatchingResult

internal class TemplateMatchingResultsRenderer(
    private val traceRenderer: Renderer<CallTrace> = ToStringRenderer,
    private val templateRenderer: Renderer<CallTemplate> = ToStringRenderer,
) : Renderer<List<TemplateMatchingResult>> {
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

    private fun StringBuilder.appendUnverifiedCallLine(call: TemplateMatchingResult.UnverifiedCall, columnSize: Int) {
        append("*".padEnd(columnSize, ' '))
        append("  ")
        appendLine(traceRenderer.render(call.trace))
    }

    private fun StringBuilder.appendTemplateLines(
        template: CallTemplate,
        trace: CallTrace?,
        index: Int,
        columnSize: Int
    ) {
        append("$index. ".padEnd(columnSize))
        append("┌ ")
        appendLine(templateRenderer.render(template))
        append(" ".padEnd(columnSize))
        append("└ ")
        if (trace != null) {
            appendLine(traceRenderer.render(trace))
        } else {
            appendLine("No matching call!")
        }
    }
}
