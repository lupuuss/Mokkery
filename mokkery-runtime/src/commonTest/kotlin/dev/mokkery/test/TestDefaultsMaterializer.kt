package dev.mokkery.test

import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.matcher.CallEntry
import dev.mokkery.internal.matcher.DefaultValuesMatcher
import dev.mokkery.internal.templating.CallTemplate

internal class TestDefaultsMaterializer(
    var calls: (CallTemplate, CallEntry) -> CallTemplate = { template, _ -> template },
    var checks: (CallTemplate, CallEntry, CallTemplate) -> Unit = { _, _, _ -> }
) : DefaultsMaterializer {
    override fun materialize(
        template: CallTemplate,
        entry: CallEntry
    ): CallTemplate = calls(template, entry)

    override fun checkNonDeterministicDefaults(
        template: CallTemplate,
        entry: CallEntry,
        materialized: CallTemplate
    ) = checks(template, entry, materialized)
}

internal fun fakeDefaultValueMatcher(): DefaultValuesMatcher = DefaultValuesMatcher(
    mask = 0,
    extractingFunction = { error("Not prepared") },
    isExtractingFunctionSuspend = false
)
