@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.internal.annotations.TemplatingLambda
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.callTracing
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.names.aliasTemplates
import dev.mokkery.internal.names.aliasTraces
import dev.mokkery.internal.names.withShorterNames
import dev.mokkery.internal.templating.createMokkeryTemplatingScope
import dev.mokkery.internal.templating.templatingRegistry
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
    val scope = createMokkeryTemplatingScope().apply(block)
    val templating = scope.templatingRegistry
    val mocks = mokkeryContext[MocksRegistry]
        ?.mocks
        .orEmpty()
        .plus(templating.mocks)
        .withShorterNames(tools.namesShortener)
    val calls = mocks
        .scopes
        .map { it.callTracing.unverified }
        .flatten()
        .sortedBy(CallTrace::orderStamp)
    tools
        .verifierFactory
        .create(mode, mocks)
        .verify(mocks.aliasTraces(calls), mocks.aliasTemplates(templating.templates))
        .forEach { mocks.getScope(it.mockId).callTracing.markVerified(it) }
}
