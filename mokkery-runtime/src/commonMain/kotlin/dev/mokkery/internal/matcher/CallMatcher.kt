package dev.mokkery.internal.matcher

import dev.mokkery.internal.signature.SignatureGenerator
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal interface CallMatcher {
    fun match(trace: CallTrace, template: CallTemplate): CallMatchResult
}

internal enum class CallMatchResult {
    NotMatching, SameReceiverMethodSignature, SameReceiverMethodOverload, SameReceiver, Matching
}

internal inline val CallMatchResult.isMatching
    get() = this == CallMatchResult.Matching

internal inline val CallMatchResult.isNotMatching
    get() = this != CallMatchResult.Matching

internal fun CallMatcher(
    signatureGenerator: SignatureGenerator = SignatureGenerator(),
): CallMatcher = CallMatcherImpl(signatureGenerator)

private class CallMatcherImpl(private val signatureGenerator: SignatureGenerator) : CallMatcher {
    override fun match(trace: CallTrace, template: CallTemplate): CallMatchResult {
        return when {
            trace.receiver != template.receiver -> CallMatchResult.NotMatching
            !trace.matchesSignatureOf(template) -> when (trace.name) {
                template.name -> CallMatchResult.SameReceiverMethodOverload
                else -> CallMatchResult.SameReceiver
            }
            trace.matchesArgsOf(template) -> CallMatchResult.Matching
            else -> CallMatchResult.SameReceiverMethodSignature
        }
    }

    private fun CallTrace.matchesSignatureOf(
        template: CallTemplate
    ) = signatureGenerator.generate(name, args) == template.signature

    private fun CallTrace.matchesArgsOf(
        template: CallTemplate
    ) = args.all { arg -> template.matchers[arg.name]?.matches(arg.value) ?: false }
}
