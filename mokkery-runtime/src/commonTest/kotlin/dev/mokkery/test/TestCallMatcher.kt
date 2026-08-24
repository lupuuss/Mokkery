package dev.mokkery.test

import dev.mokkery.internal.matcher.CallEntry
import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.templating.CallTemplate

internal class TestCallMatcher(
    var calls: (template: CallTemplate, entry: CallEntry) -> CallMatchResult = { _, _ -> CallMatchResult.NotMatching }
): CallMatcher {
    private val _recordedCalls = mutableListOf<Pair<CallTemplate, CallEntry>>()
    val recordedCalls: List<Pair<CallTemplate, CallEntry>> = _recordedCalls

    fun returns(value: CallMatchResult) {
        calls = { _, _ -> value }
    }

    fun returns(value: Boolean) {
        returns(if (value) CallMatchResult.Matching else CallMatchResult.NotMatching)
    }

    fun returnsMany(vararg values: Boolean) {
        returnsMany(*values.map { if (it) CallMatchResult.Matching else CallMatchResult.NotMatching }.toTypedArray())
    }

    fun returnsMany(vararg values: CallMatchResult) {
        val valuesQueue = values.toMutableList()
        calls = { _, _ -> valuesQueue.removeAt(0) }
    }

    override fun match(template: CallTemplate, entry: CallEntry): CallMatchResult {
        _recordedCalls.add(template to entry)
        return calls(template, entry)
    }

    override fun areMatching(
        template: CallTemplate,
        entry: CallEntry
    ): Boolean {
        _recordedCalls.add(template to entry)
        return calls(template, entry).isMatching
    }
}
