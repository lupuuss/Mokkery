package dev.mokkery.test

import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.matcher.CallEntry
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class TestDefaultsMaterializer(
    var calls: (CallTemplate, CallEntry) -> CallTemplate = { template, _ -> template }
) : DefaultsMaterializer {
    override fun materialize(
        template: CallTemplate,
        entry: CallEntry
    ): CallTemplate = calls(template, entry)
}

internal fun fakeDefaultValueMatcher(): DefaultValuesMatcher = DefaultValuesMatcher(
    mask = 0,
    extractingFunction = { error("Not prepared") },
    isExtractingFunctionSuspend = false
)
