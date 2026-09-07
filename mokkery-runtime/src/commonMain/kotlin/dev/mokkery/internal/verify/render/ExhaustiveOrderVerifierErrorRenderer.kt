package dev.mokkery.internal.verify.render

import dev.mokkery.rendering.Renderer
import dev.mokkery.internal.verify.ExhaustiveOrderVerifier
import dev.mokkery.rendering.MokkeryRenderingScope

internal object ExhaustiveOrderVerifierErrorRenderer : Renderer<ExhaustiveOrderVerifier.Error> {

    override val key get() = VerifyRendering.exhaustiveOrderVerifierError

    context(scope: MokkeryRenderingScope)
    override fun render(value: ExhaustiveOrderVerifier.Error) = buildString {
        appendLine("Expected strict order of calls without unverified ones, but not satisfied!")
        append(scope.templateMatchingResults.render(value.results))
    }
}
