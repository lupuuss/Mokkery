package dev.mokkery.test

import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.matcher.DefaultValueMatcher

internal class TestDefaultsMaterializer(
    var calls: (CallTrace, CallTemplate) -> CallTemplate = { _, template -> template }
) : DefaultsMaterializer {
    override fun materialize(
        trace: CallTrace,
        template: CallTemplate
    ): CallTemplate = calls(trace, template)
}

internal fun <T> fakeDefaultValueMatcher(): DefaultValueMatcher<T> = DefaultValueMatcher(
    mask = 0,
    caller = { error("Not prepared") },
    isSuspend = false
)
