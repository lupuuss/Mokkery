package dev.mokkery.internal.verify.results

import dev.mokkery.internal.calls.CallMatchResult
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace

internal data class TemplateGroupedMatchingResults(
    val template: CallTemplate,
    val calls: Map<CallMatchResult, List<CallTrace>>
)
