@file:Suppress( "UNUSED_PARAMETER", "unused")
package dev.mokkery

import dev.mokkery.internal.MokkeryInterceptorScope
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.internal.MokkerySpy
import dev.mokkery.internal.ObjectNotSpiedException
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode

/**
 * Asserts that calls sequence defined in [block] satisfies given [mode].
 *
 * Provided [block] **must** be a lambda and all mock calls **must** occur directly inside it. Extracting [block]
 * content to functions is prohibited.
 */
public fun verify(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

/**
 * Just like [verify], but allows suspendable function calls.
 */
public fun verifySuspend(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: suspend ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

/**
 * Asserts that all given [mocks] have all their registered calls verified with [verify] or [verifySuspend].
 */
public fun verifyNoMoreCalls(vararg mocks: Any) {
    mocks.forEach { mock ->
        val tracing = mock
            .let { it as? MokkeryInterceptorScope }
            ?.interceptor
            ?.let { it as? MokkerySpy }
            ?.callTracing ?: throw ObjectNotSpiedException(mock)
        if (tracing.unverified.isNotEmpty()) {
            throw AssertionError("Unverified calls for $mock: \n\t${tracing.unverified.joinToString("\n\t")}")
        }
    }
}
