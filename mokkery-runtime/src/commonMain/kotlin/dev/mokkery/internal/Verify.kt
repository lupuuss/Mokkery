@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.callTracing
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.names.aliasTemplates
import dev.mokkery.internal.names.aliasTraces
import dev.mokkery.internal.names.withShorterNames
import dev.mokkery.internal.templating.TemplatingScopeImpl
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.verify.VerifyMode

internal fun MokkerySuiteScope.internalVerifySuspend(
    mode: VerifyMode,
    block: suspend TemplatingScopeImpl.() -> Unit
) = internalVerify(mode) { runSuspension { block() } }

internal fun MokkerySuiteScope.internalVerify(
    mode: VerifyMode,
    block: TemplatingScopeImpl.() -> Unit
) {
    val templating = TemplatingScopeImpl().apply(block)
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
