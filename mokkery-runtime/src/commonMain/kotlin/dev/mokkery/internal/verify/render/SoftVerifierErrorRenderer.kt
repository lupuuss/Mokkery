package dev.mokkery.internal.verify.render

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.verify.SoftVerifier
import dev.mokkery.rendering.MokkeryRenderingScope

internal object SoftVerifierErrorRenderer : Renderer<SoftVerifier.Error> {

    override val key get() = VerifyRendering.softVerifierError

    context(scope: MokkeryRenderingScope)
    override fun render(value: SoftVerifier.Error) = buildString {
        val atLeast = value.expectedAtLeast
        val atMost = value.expectedAtMost
        val callsCount = value.templateMatchingResults.calls[CallMatchResult.Matching]?.size ?: 0
        append("Expected ")
        val callsExpectations = when {
            atLeast == atMost -> "exactly $atLeast calls"
            atLeast != 1 && atMost != Int.MAX_VALUE -> "calls count to be in range $atLeast..$atMost"
            atLeast != 1 -> "at least $atLeast calls"
            atMost != Int.MAX_VALUE -> "at most $atMost calls"
            else -> "any call"
        }
        append(callsExpectations)
        if (callsCount == 0) {
            append(", but no matching calls")
        } else {
            append(", but $callsCount occurred")
        }
        appendLine(" for ${callTemplateRenderer.render(value.templateMatchingResults.template)}!")
        append(templateGroupedMatchingResults.render(value.templateMatchingResults))
    }

}
