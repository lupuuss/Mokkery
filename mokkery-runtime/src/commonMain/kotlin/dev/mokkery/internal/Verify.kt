@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.TemplatingScope
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.names.createGroupMockReceiverShortener
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode

internal fun MokkerySuiteScope.internalVerifySuspend(
    scope: TemplatingScope,
    mode: VerifyMode,
    block: suspend ArgMatchersScope.() -> Unit
) = internalVerify(scope, mode) { runSuspension { block() } }

internal fun MokkerySuiteScope.internalVerify(
    templating: TemplatingScope,
    mode: VerifyMode,
    block: ArgMatchersScope.() -> Unit
) {
    val verifier = tools.verifierFactory.create(mode)
    val result = runCatching { block(templating) }
    val exception = result.exceptionOrNull()
    if (exception != null && exception !is DefaultNothingException) {
        templating.release()
        throw exception
    }
    val instanceLookup = tools.instanceLookup
    val allMokkeryInstances = mokkeryContext[MocksRegistry]
        ?.mocks
        ?.mapNotNull { instanceLookup.resolve(it) as? MokkeryMockInstance }
        .orEmpty()
        .plus(templating.mocks)
    val spyInterceptors = allMokkeryInstances.associate { it.id to it.interceptor }
    val calls = spyInterceptors
        .values
        .map { it.callTracing.unverified }
        .flatten()
        .sortedBy(CallTrace::orderStamp)
    val shortener = tools.createGroupMockReceiverShortener()
    shortener.prepare(calls, templating.templates)
    try {
        verifier
            .verify(shortener.shortenTraces(calls), shortener.shortenTemplates(templating.templates))
            .map(shortener::getOriginalTrace)
            .forEach { spyInterceptors.getValue(it.receiver).callTracing.markVerified(it) }
    } finally {
        templating.release()
    }
}
