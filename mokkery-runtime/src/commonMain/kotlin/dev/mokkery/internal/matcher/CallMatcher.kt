package dev.mokkery.internal.matcher

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal interface CallMatcher {
    fun matches(trace: CallTrace, template: CallTemplate): Boolean
}


internal fun CallMatcher(): CallMatcher = CallMatcherImpl()

private class CallMatcherImpl : CallMatcher {
    override fun matches(trace: CallTrace, template: CallTemplate): Boolean {
        return trace.receiver == template.receiver &&
                trace.signature == template.signature &&
                trace.args.all { arg -> template.matchers[arg.name]?.matches(arg.value) ?: false }
    }
}
