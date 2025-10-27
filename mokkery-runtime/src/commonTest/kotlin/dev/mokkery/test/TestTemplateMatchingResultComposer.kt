package dev.mokkery.test

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer

internal class TestTemplateMatchingResultComposer(
    var calls: (traces: List<CallTrace>, templates: List<CallTemplate>) -> List<TemplateMatchingResult> =
        { _, _ -> emptyList() },
) : TemplateMatchingResultsComposer {

    private val _recordedCalls = mutableListOf<Pair<List<CallTrace>, List<CallTemplate>>>()
    val recordedCalls: List<Pair<List<CallTrace>, List<CallTemplate>>> = _recordedCalls

    fun returns(value: List<TemplateMatchingResult>) {
        calls = { _, _ -> value }
    }

    override fun compose(traces: List<CallTrace>, templates: List<CallTemplate>): List<TemplateMatchingResult> {
        _recordedCalls.add(traces to templates)
        return calls(traces, templates)
    }
}
