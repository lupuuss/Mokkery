package dev.mokkery.internal.matcher

import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.templating.CallTemplate

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
    defaultsMaterializer: DefaultsMaterializer
): CallMatcher = CallMatcherImpl(defaultsMaterializer)

private class CallMatcherImpl(
    private val defaultsMaterializer: DefaultsMaterializer
) : CallMatcher {

    override fun match(trace: CallTrace, template: CallTemplate): CallMatchResult = when {
        trace.instanceId != template.instanceId -> CallMatchResult.NotMatching
        trace.name != template.name -> CallMatchResult.SameReceiver
        !(trace hasTheSameParameters template) -> CallMatchResult.SameReceiverMethodOverload
        trace.matchesArgsOf(template) -> CallMatchResult.Matching
        else -> CallMatchResult.SameReceiverMethodSignature
    }

    private fun CallTrace.matchesArgsOf(template: CallTemplate): Boolean {
        val materializedTemplate = defaultsMaterializer.materialize(this, template)
        return args.all { arg -> materializedTemplate.matchers[arg.parameter.name]?.matches(arg.value) == true }
    }

    private infix fun CallTrace.hasTheSameParameters(template: CallTemplate): Boolean {
        val args = args
        val parameters = template.parameters
        if (args.size != template.parameters.size) return false
        for (i in 0..<args.size) {
            if (args[i].parameter != parameters[i]) return false
        }
        return true
    }
}
