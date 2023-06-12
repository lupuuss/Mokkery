@file:Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER", "unused")
package dev.mokkery

import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher.CallTemplateRegistry
import dev.mokkery.verify.ExhaustiveSoftVerifier
import dev.mokkery.verify.ExhaustiveOrderVerifier
import dev.mokkery.verify.NotVerifier
import dev.mokkery.verify.SoftVerifier
import dev.mokkery.verify.OrderVerifier
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyMode.Companion.soft

public fun verify(
    mode: VerifyMode = defaultMode,
    block: ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

public suspend fun verifySuspend(
    mode: VerifyMode = defaultMode,
    block: suspend ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

public fun verifyNoMoreCalls(vararg mocks: Any) {
    mocks.forEach {
        if (it !is MokkeryScope) throw ObjectNotMockedMockedExcpetion(it)
        if (it.mokkery.unverifiedTraces.isNotEmpty()) {
            throw AssertionError("Unverified calls for $it: \n\t${it.mokkery.unverifiedTraces.joinToString("\n\t")}")
        }
    }
}

internal val defaultMode = soft

internal fun internalVerify(
    mocks: Array<Any>,
    mode: VerifyMode = defaultMode,
    block: ArgMatchersScope.() -> Unit
) = internalBaseVerify(mocks, mode, block)

internal suspend fun internalVerifySuspend(
    mocks: Array<Any>,
    mode: VerifyMode = defaultMode,
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
    val registry = CallTemplateRegistry()
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

