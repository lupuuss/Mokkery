@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.internal.coroutines.runSuspension
import dev.mokkery.internal.matcher.ArgMatchersScope
import dev.mokkery.internal.templating.TemplatingContext
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.ExhaustiveOrderVerifier
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.internal.verify.NotVerifier
import dev.mokkery.internal.verify.OrderVerifier
import dev.mokkery.internal.verify.SoftVerifier
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode

internal fun internalVerify(
    spied: Array<Any>,
    mode: VerifyMode = VerifyMode.default,
    block: ArgMatchersScope.() -> Unit
) = internalBaseVerify(spied, mode, block)

internal fun internalVerifySuspend(
    spied: Array<Any>,
    mode: VerifyMode = VerifyMode.default,
    block: suspend ArgMatchersScope.() -> Unit
) = internalBaseVerify(spied, mode) {
    runSuspension { block() }
}

internal inline fun internalBaseVerify(
    spied: Array<Any>,
    mode: VerifyMode,
    block: ArgMatchersScope.() -> Unit
) {
    val spyInterceptors = spied
        .toSet()
        .filterIsInstance<MokkerySpyScope>()
        .associate { it.id to it.interceptor }
    val context = TemplatingContext()
    spyInterceptors.values.forEach { it.templating.start(context) }
    try {
        block(ArgMatchersScope(context))
    } catch (e: DefaultNothingException) {
        // function with Nothing return type was called
    } finally {
        spyInterceptors.values.forEach { it.templating.stop() }
    }
    val calls = spyInterceptors
        .values
        .map { it.callTracing.unverified }
        .flatten()
        .sortedBy(CallTrace::orderStamp)
    val verifier = when (mode) {
        VerifyMode.Order -> OrderVerifier
        VerifyMode.ExhaustiveOrder -> ExhaustiveOrderVerifier
        VerifyMode.ExhaustiveSoft -> ExhaustiveSoftVerifier
        VerifyMode.Not -> NotVerifier
        is VerifyMode.Soft -> SoftVerifier(mode.atLeast, mode.atMost)
    }
    verifier.verify(calls, context.templates).forEach {
        spyInterceptors.getValue(it.receiver).callTracing.markVerified(it)
    }
}
