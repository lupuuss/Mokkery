@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.callTracing
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.names.createGroupMockReceiverShortener
import dev.mokkery.internal.templating.TemplatingScope
import dev.mokkery.internal.utils.getScope
import dev.mokkery.internal.utils.orEmpty
import dev.mokkery.internal.utils.plus
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.verify.VerifyMode

internal fun MokkerySuiteScope.internalVerifySuspend(
    mode: VerifyMode,
    block: suspend TemplatingScope.() -> Unit
) = internalVerify(mode) { runSuspension { block() } }

internal fun MokkerySuiteScope.internalVerify(
    mode: VerifyMode,
    block: TemplatingScope.() -> Unit
) {
    val verifier = tools.verifierFactory.create(mode)
    val templating = TemplatingScope().apply(block)
    val mocks = mokkeryContext[MocksRegistry]?.mocks.orEmpty() + templating.mocks
    val calls = mocks
        .scopes
        .map { it.callTracing.unverified }
        .flatten()
        .sortedBy(CallTrace::orderStamp)
    val shortener = tools.createGroupMockReceiverShortener()
    shortener.prepare(calls, templating.templates)
    verifier
        .verify(shortener.shortenTraces(calls), shortener.shortenTemplates(templating.templates))
        .map(shortener::getOriginalTrace)
        .forEach { mocks.getScope(it.mockId).callTracing.markVerified(it) }
}
