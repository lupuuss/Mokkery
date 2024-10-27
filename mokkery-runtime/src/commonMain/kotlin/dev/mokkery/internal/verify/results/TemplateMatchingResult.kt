package dev.mokkery.internal.verify.results

import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace

internal sealed interface TemplateMatchingResult {

    data class UnverifiedCall(val trace: CallTrace) : TemplateMatchingResult

    data class Matching(val trace: CallTrace, val template: CallTemplate) : TemplateMatchingResult

    data class NoMatch(val template: CallTemplate) : TemplateMatchingResult
}

