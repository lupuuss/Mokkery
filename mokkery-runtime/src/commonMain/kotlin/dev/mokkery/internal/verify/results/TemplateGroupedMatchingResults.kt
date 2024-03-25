package dev.mokkery.internal.verify.results

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal data class TemplateGroupedMatchingResults(
    val template: CallTemplate,
    val calls: Map<CallMatchResult, List<CallTrace>>
)
