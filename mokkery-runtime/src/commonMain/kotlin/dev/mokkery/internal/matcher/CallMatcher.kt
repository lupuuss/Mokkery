package dev.mokkery.internal.matcher

import dev.mokkery.internal.signature.SignatureGenerator
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal interface CallMatcher {
    fun matches(trace: CallTrace, template: CallTemplate): Boolean
}


internal fun CallMatcher(
    signatureGenerator: SignatureGenerator = SignatureGenerator(),
): CallMatcher = CallMatcherImpl(signatureGenerator)

private class CallMatcherImpl(private val signatureGenerator: SignatureGenerator) : CallMatcher {
    override fun matches(trace: CallTrace, template: CallTemplate): Boolean {
        return trace.receiver == template.receiver &&
                signatureGenerator.generate(trace.name, trace.args) == template.signature &&
                trace.args.all { arg -> template.matchers[arg.name]?.matches(arg.value) ?: false }
    }
}
