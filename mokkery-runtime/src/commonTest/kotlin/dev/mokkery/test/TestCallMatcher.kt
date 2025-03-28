package dev.mokkery.test

import dev.mokkery.internal.calls.CallMatchResult
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace

internal class TestCallMatcher(
    var calls: (trace: CallTrace, template: CallTemplate) -> CallMatchResult = { _, _ -> CallMatchResult.NotMatching }
): CallMatcher {
    private val _recordedCalls = mutableListOf<Pair<CallTrace, CallTemplate>>()
    val recordedCalls: List<Pair<CallTrace, CallTemplate>> = _recordedCalls

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
        calls = { _, _ -> valuesQueue.removeFirst() }
    }

    override fun match(trace: CallTrace, template: CallTemplate): CallMatchResult {
        _recordedCalls.add(trace to template)
        return calls(trace, template)
    }
}
