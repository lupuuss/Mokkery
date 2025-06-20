package dev.mokkery.internal.calls

import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.names.SignatureGenerator
import dev.mokkery.internal.names.generate

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
    signatureGenerator: SignatureGenerator,
    defaultsMaterializer: DefaultsMaterializer
): CallMatcher = CallMatcherImpl(signatureGenerator, defaultsMaterializer)

private class CallMatcherImpl(
    private val signatureGenerator: SignatureGenerator,
    private val defaultsMaterializer: DefaultsMaterializer
) : CallMatcher {

    override fun match(trace: CallTrace, template: CallTemplate): CallMatchResult {
        return when {
            trace.mockId != template.mockId -> CallMatchResult.NotMatching
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

    private fun CallTrace.matchesArgsOf(template: CallTemplate): Boolean {
        val materializedTemplate = defaultsMaterializer.materialize(this, template)
        return args.all { arg -> materializedTemplate.matchers[arg.parameter.name]?.matches(arg.value) == true }
    }
}
