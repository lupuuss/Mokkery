@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.TemplatingScope
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.names.createGroupMockReceiverShortener
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode

internal fun internalVerifySuspend(
    scope: TemplatingScope,
    mode: VerifyMode,
    block: suspend ArgMatchersScope.() -> Unit
) = internalVerify(scope, mode) { runSuspension { block() } }

internal fun internalVerify(
    scope: TemplatingScope,
    mode: VerifyMode,
    block: ArgMatchersScope.() -> Unit
) {
    val tools = GlobalMokkeryScope.tools
    val verifier = tools.verifierFactory.create(mode)
    val result = runCatching { block(scope) }
    val exception = result.exceptionOrNull()
    if (exception != null && exception !is DefaultNothingException) {
        scope.release()
        throw exception
    }
    val spyInterceptors = scope.mocks.associate { it.id to it.interceptor }
    val calls = spyInterceptors
        .values
        .map { it.callTracing.unverified }
        .flatten()
        .sortedBy(CallTrace::orderStamp)
    val shortener = tools.createGroupMockReceiverShortener()
    shortener.prepare(calls, scope.templates)
    try {
        verifier
            .verify(shortener.shortenTraces(calls), shortener.shortenTemplates(scope.templates))
            .map(shortener::getOriginalTrace)
            .forEach { spyInterceptors.getValue(it.receiver).callTracing.markVerified(it) }
    } finally {
        scope.release()
    }
}
