@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.internal.annotations.TemplatingLambda
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.names.aliasTemplates
import dev.mokkery.internal.names.aliasTraces
import dev.mokkery.internal.names.withShorterNames
import dev.mokkery.internal.templating.createTemplatingScope
import dev.mokkery.internal.templating.templatingRegistry
import dev.mokkery.internal.tracing.callTracing
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.templating.MokkeryTemplatingScope
import dev.mokkery.verify.VerifyMode

internal fun MokkerySuiteScope.internalVerifySuspend(
    mode: VerifyMode,
    block: @TemplatingLambda suspend MokkeryTemplatingScope.() -> Unit
) = internalVerify(mode) { runSuspension { block() } }

internal fun MokkerySuiteScope.internalVerify(
    mode: VerifyMode,
    block: @TemplatingLambda MokkeryTemplatingScope.() -> Unit
) {
    val scope = createTemplatingScope().apply(block)
    val templating = scope.templatingRegistry
    val collection = mokkeryContext[MokkeryInstancesRegistry]
        ?.collection
        .orEmpty()
        .plus(templating.collection)
        .withShorterNames(tools.namesShortener)
    val calls = collection
        .scopes
        .map { it.callTracing.unverified }
        .flatten()
        .sortedBy(CallTrace::orderStamp)
    tools
        .verifierFactory
        .create(mode, collection)
        .verify(collection.aliasTraces(calls), collection.aliasTemplates(templating.templates))
        .forEach { collection.getScope(it.instanceId).callTracing.markVerified(it) }
}
