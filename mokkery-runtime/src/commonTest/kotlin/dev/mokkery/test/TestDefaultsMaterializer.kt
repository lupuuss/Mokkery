package dev.mokkery.test

import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class TestDefaultsMaterializer(
    var calls: (CallTrace, CallTemplate) -> CallTemplate = { _, template -> template }
) : DefaultsMaterializer {
    override fun materialize(
        trace: CallTrace,
        template: CallTemplate
    ): CallTemplate = calls(trace, template)
}

internal fun fakeDefaultValueMatcher(): DefaultValuesMatcher = DefaultValuesMatcher(
    mask = 0,
    extractingFunction = { error("Not prepared") },
    isExtractingFunctionSuspend = false
)
