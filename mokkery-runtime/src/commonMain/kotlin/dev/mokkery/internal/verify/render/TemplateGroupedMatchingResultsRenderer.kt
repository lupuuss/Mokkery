package dev.mokkery.internal.verify.render

import dev.mokkery.internal.calls.CallMatchResult
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.ToStringRenderer
import dev.mokkery.internal.render.indentationString
import dev.mokkery.internal.render.withIndentation
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults

internal class TemplateGroupedMatchingResultsRenderer(
    private val matchersFailuresRenderer: Renderer<Pair<CallTemplate, CallTrace>>,
    private val traceRenderer: Renderer<CallTrace> = ToStringRenderer,
    private val indentation: Int = 2,
) : Renderer<TemplateGroupedMatchingResults> {

    private val traceIndentationString = indentationString(indentation)

    override fun render(value: TemplateGroupedMatchingResults): String = buildString {
        val (template, results) = value
        appendLine("Results for ${value.template.mockId}:")
        if (results.all { (_, value) -> value.isEmpty() }) {
            appendLine("# No calls to this mock!")
            return@buildString
        }
        appendOptionalGroup(results[CallMatchResult.Matching], "Matching calls")
        appendOptionalGroup(results[CallMatchResult.SameReceiverMethodSignature], "Calls to the same method with failing matchers") { calls ->
            calls.forEach {
                append(traceIndentationString)
                appendLine(traceRenderer.render(it))
                append(matchersFailuresRenderer.render(template to it).withIndentation(2 * indentation))
            }
        }
        appendOptionalGroup(results[CallMatchResult.SameReceiverMethodOverload], "Calls to the same overload")
        appendOptionalGroup(results[CallMatchResult.SameReceiver], "Other calls to this mock")
    }
    private inline fun StringBuilder.appendOptionalGroup(
        traces: List<CallTrace>?,
        label: String,
        renderTraces: StringBuilder.(List<CallTrace>) -> Unit = { appendRenderedTracesFrom(it) }
    ) {
        if (traces?.isNotEmpty() == true) {
            append("# ")
            append(label)
            appendLine(":")
            renderTraces(traces)
        }
    }

    private fun StringBuilder.appendRenderedTracesFrom(traces: List<CallTrace>) {
        traces.forEach {
            append(traceIndentationString)
            appendLine(traceRenderer.render(it))
        }
    }
}
