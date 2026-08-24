package dev.mokkery.internal.verify.render

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.rendering.Renderer
import dev.mokkery.internal.rendering.callEntryRenderer
import dev.mokkery.internal.rendering.indentationString
import dev.mokkery.internal.rendering.instanceIdRenderer
import dev.mokkery.internal.rendering.withIndentation
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.rendering.MokkeryRenderingScope

internal object TemplateGroupedMatchingResultsRenderer : Renderer<TemplateGroupedMatchingResults> {

    private const val INDENT: Int = 2

    private val traceIndentationString = indentationString(INDENT)

    override val key get() = VerifyRendering.templateGroupedMatchingResults

    context(scope: MokkeryRenderingScope)
    override fun render(value: TemplateGroupedMatchingResults): String = buildString {
        val (template, results) = value
        appendLine("Results for ${scope.instanceIdRenderer.render(value.template.instanceId)}:")
        if (results.all { (_, value) -> value.isEmpty() }) {
            appendLine("# No calls to this mock!")
            return@buildString
        }
        appendOptionalGroup(results[CallMatchResult.Matching], "Matching calls")
        appendOptionalGroup(results[CallMatchResult.SameReceiverMethodSignature], "Calls to the same method with failing matchers") { calls ->
            calls.forEach {
                append(traceIndentationString)
                appendLine(scope.callEntryRenderer.render(it))
                append(scope.matcherStatus.render(template to it).withIndentation(2 * INDENT))
            }
        }
        appendOptionalGroup(results[CallMatchResult.SameReceiverMethodOverload], "Calls to the same overload")
        appendOptionalGroup(results[CallMatchResult.SameReceiver], "Other calls to this mock")
    }

    context(scope: MokkeryRenderingScope)
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

    context(scope: MokkeryRenderingScope)
    private fun StringBuilder.appendRenderedTracesFrom(traces: List<CallTrace>) {
        traces.forEach {
            append(traceIndentationString)
            appendLine(scope.callEntryRenderer.render(it))
        }
    }
}
