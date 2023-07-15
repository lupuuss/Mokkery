package dev.mokkery.internal.matcher

import dev.mokkery.internal.CallContext
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.matches

internal interface CallMatcher {
    fun matches(trace: CallTrace, template: CallTemplate): Boolean
}


internal fun CallMatcher(): CallMatcher = CallMatcherImpl()

private class CallMatcherImpl : CallMatcher {
    override fun matches(trace: CallTrace, template: CallTemplate): Boolean {
        return trace matches template
    }
}
