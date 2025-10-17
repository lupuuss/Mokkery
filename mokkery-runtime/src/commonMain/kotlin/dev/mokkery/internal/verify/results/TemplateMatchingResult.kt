package dev.mokkery.internal.verify.results

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal sealed interface TemplateMatchingResult {

    data class UnverifiedCall(val trace: CallTrace) : TemplateMatchingResult

    data class Matching(val trace: CallTrace, val template: CallTemplate) : TemplateMatchingResult

    data class NoMatch(val template: CallTemplate) : TemplateMatchingResult
}

