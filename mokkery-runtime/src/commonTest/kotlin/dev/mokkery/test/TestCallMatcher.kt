package dev.mokkery.test

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class TestCallMatcher(
    var calls: (trace: CallTrace, template: CallTemplate) -> Boolean = { _, _ -> false }
): CallMatcher {
    private val _recordedCalls = mutableListOf<Pair<CallTrace, CallTemplate>>()
    val recordedCalls: List<Pair<CallTrace, CallTemplate>> = _recordedCalls

    fun returns(value: Boolean) {
        calls = { _, _ -> value }
    }

    fun returnsMany(vararg values: Boolean) {
        val valuesQueue = values.toMutableList()
        calls = { _, _ -> valuesQueue.removeFirst() }
    }

    override fun matches(trace: CallTrace, template: CallTemplate): Boolean {
        _recordedCalls.add(trace to template)
        return calls(trace, template)
    }
}
