package dev.mokkery.internal.verify

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal sealed interface Verifier {

    fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): Result

    sealed interface Result {
        data class Success(val verified: List<CallTrace>) : Result
        data class Failure(val error: Error) : Result
    }

    sealed interface Error
}

