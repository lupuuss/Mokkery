package dev.mokkery.internal.calls

import dev.mokkery.internal.names.SignatureGenerator

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

internal fun CallMatcher(signatureGenerator: SignatureGenerator): CallMatcher = CallMatcherImpl(signatureGenerator)

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
    ) = args.all { arg -> template.matchers[arg.parameter.name]?.matches(arg.value) == true }
}
