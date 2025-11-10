@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.templating.createTemplatingScope
import dev.mokkery.internal.templating.templatingRegistry
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.callTracing
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.templating.MokkeryTemplatingScope
import dev.mokkery.verify.VerifyMode

internal fun MokkeryScope.internalVerifySuspend(
    mode: VerifyMode,
    block: @Templating suspend MokkeryTemplatingScope.() -> Unit
) = internalVerify(mode) { runSuspension { block() } }

internal fun MokkeryScope.internalVerify(
    mode: VerifyMode,
    block: @Templating MokkeryTemplatingScope.() -> Unit
) {
    val scope = createTemplatingScope().apply(block)
    val templating = scope.templatingRegistry
    val collection = mokkeryContext[MokkeryInstancesRegistry]
        ?.collection
        .orEmpty()
        .plus(templating.collection)
    val calls = collection
        .scopes
        .map { it.callTracing.unverified }
        .flatten()
        .sortedBy(CallTrace::orderStamp)
    tools
        .verifierFactory
        .create(mode, collection)
        .verify(calls, templating.templates)
        .forEach { collection.getScope(it.instanceId).callTracing.markVerified(it) }
}
