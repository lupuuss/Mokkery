package dev.mokkery.internal.matcher

import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.functions
import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.getScope
import dev.mokkery.internal.templating.CallTemplate

internal val MokkeryInstanceScope.callMatcher: CallMatcher
    get() = mokkeryContext.require(CallMatcher)

internal val MokkeryCallScope.callMatcher: CallMatcher
    get() = mokkeryContext.require(CallMatcher)

internal interface CallMatcher : MokkeryContext.Element {

    override val key: Key get() = Key

    fun match(template: CallTemplate, entry: CallEntry): CallMatchResult

    fun areMatching(template: CallTemplate, entry: CallEntry): Boolean

    companion object Key : MokkeryContext.Key<CallMatcher>

    fun interface Factory {

        fun create(collection: MokkeryCollection): CallMatcher

        companion object {

            fun default(materializer: DefaultsMaterializer.Factory) = Factory {
                CallMatcher(it, materializer.create(it))
            }
        }
    }
}

internal enum class CallMatchResult {
    NotMatching, SameReceiverMethodSignature, SameReceiverMethodOverload, SameReceiver, Matching
}

internal inline val CallMatchResult.isMatching
    get() = this == CallMatchResult.Matching

internal fun CallMatcher(
    collection: MokkeryCollection,
    defaultsMaterializer: DefaultsMaterializer,
): CallMatcher = CallMatcherImpl(collection, defaultsMaterializer)

private class CallMatcherImpl(
    private val collection: MokkeryCollection,
    private val defaultsMaterializer: DefaultsMaterializer,
) : CallMatcher {

    override fun match(template: CallTemplate, entry: CallEntry): CallMatchResult = when {
        template.instanceId != entry.instanceId -> CallMatchResult.NotMatching
        template.functionId != entry.functionId -> template.functionMismatchResultAgainst(entry)
        template.matchesArgsFrom(entry) -> CallMatchResult.Matching
        else -> CallMatchResult.SameReceiverMethodSignature
    }

    override fun areMatching(
        template: CallTemplate,
        entry: CallEntry
    ): Boolean = template.instanceId == entry.instanceId
            &&  template.functionId == entry.functionId
            && template.matchesArgsFrom(entry)

    private fun CallTemplate.matchesArgsFrom(entry: CallEntry): Boolean {
        val materialized = defaultsMaterializer.materialize(this, entry)
        val matchers = materialized.matchers
        val args = entry.args
        if (matchers.size != args.size) return false
        for (index in args.indices) {
            if (matchers[index].matches(args[index])) continue
            if (matchers[index] !is MaterializedDefaultValueMatcher) return false
            defaultsMaterializer.checkNonDeterministicDefaults(this, entry, materialized)
            return false
        }
        return true
    }

    private fun CallTemplate.functionMismatchResultAgainst(entry: CallEntry): CallMatchResult {
        val scope = collection.getScope(entry.instanceId)
        return when (scope.functions[entry.functionId].name) {
            scope.functions[functionId].name -> CallMatchResult.SameReceiverMethodOverload
            else -> CallMatchResult.SameReceiver
        }
    }
}
