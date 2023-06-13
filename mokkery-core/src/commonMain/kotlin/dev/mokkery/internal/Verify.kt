@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.internal.tracing.CallTemplateTracer
import dev.mokkery.internal.verify.ExhaustiveOrderVerifier
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.internal.verify.NotVerifier
import dev.mokkery.internal.verify.OrderVerifier
import dev.mokkery.internal.verify.SoftVerifier
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode


internal fun internalVerify(
    mocks: Array<Any>,
    mode: VerifyMode = VerifyMode.default,
    block: ArgMatchersScope.() -> Unit
) = internalBaseVerify(mocks, mode, block)

internal suspend fun internalVerifySuspend(
    mocks: Array<Any>,
    mode: VerifyMode = VerifyMode.default,
    block: suspend ArgMatchersScope.() -> Unit
) = internalBaseVerify(mocks, mode) { block() }

internal inline fun internalBaseVerify(
    mocks: Array<Any>,
    mode: VerifyMode,
    block: ArgMatchersScope.() -> Unit
) {
    val casted = mocks
        .toSet()
        .filterIsInstance<MokkeryScope>()
        .map(MokkeryScope::mokkery)
    val registry = CallTemplateTracer()
    try {
        casted.forEach { it.startTemplateRegistering(registry) }
        block(ArgMatchersScope(registry))
    } finally {
        casted.forEach { it.stopTemplateRegistering() }
    }
    val verifier = when (mode) {
        VerifyMode.Order -> OrderVerifier
        VerifyMode.ExhaustiveOrder -> ExhaustiveOrderVerifier
        VerifyMode.ExhaustiveSoft -> ExhaustiveSoftVerifier
        VerifyMode.Not -> NotVerifier
        is VerifyMode.Soft -> SoftVerifier(mode.atLeast, mode.atMost)
    }
    val calls = casted.flatMap(Mokkery::unverifiedTraces)
    verifier.verify(calls, registry.templates).forEach {
        it.mokkery.markVerified(it)
    }
}
