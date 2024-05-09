@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.internal.coroutines.runSuspension
import dev.mokkery.internal.names.GroupMockReceiverShortener
import dev.mokkery.internal.templating.TemplatingScope
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.ExhaustiveOrderVerifier
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.internal.verify.NotVerifier
import dev.mokkery.internal.verify.OrderVerifier
import dev.mokkery.internal.verify.SoftVerifier
import dev.mokkery.internal.verify.Verifier
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.ExhaustiveOrderVerifyMode
import dev.mokkery.verify.ExhaustiveSoftVerifyMode
import dev.mokkery.verify.NotVerifyMode
import dev.mokkery.verify.OrderVerifyMode
import dev.mokkery.verify.SoftVerifyMode
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
    val verifier = when (mode) {
        OrderVerifyMode -> OrderVerifier()
        ExhaustiveOrderVerifyMode -> ExhaustiveOrderVerifier()
        ExhaustiveSoftVerifyMode -> ExhaustiveSoftVerifier()
        NotVerifyMode -> NotVerifier()
        is SoftVerifyMode -> SoftVerifier(mode.atLeast, mode.atMost)
    }
    internalBaseVerify(scope, verifier, block)
}

internal fun internalBaseVerify(scope: TemplatingScope, verifier: Verifier, block: ArgMatchersScope.() -> Unit) {
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
    val shortener = GroupMockReceiverShortener()
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
