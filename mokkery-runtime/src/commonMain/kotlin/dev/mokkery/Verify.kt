@file:Suppress( "UNUSED_PARAMETER", "unused")
package dev.mokkery

import dev.mokkery.internal.MokkeryMockInstance
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.dynamic.MokkeryInstanceLookup
import dev.mokkery.internal.failAssertion
import dev.mokkery.internal.interceptor
import dev.mokkery.internal.names.GroupMockReceiverShortener
import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode

/**
 * Asserts that calls sequence defined in [block] satisfies given [mode].
 *
 * Each verification is performed only on unverified calls. In result repeated verifications may give different results.
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
            .let { MokkeryInstanceLookup.current.resolve(it) as? MokkeryMockInstance }
            ?.interceptor
            ?.callTracing ?: throw ObjectNotMockedException(mock)
        if (tracing.unverified.isNotEmpty()) {
            failAssertion {
                val renderer = PointListRenderer<CallTrace>()
                val shortener = GroupMockReceiverShortener()
                shortener.prepare(tracing.unverified, emptyList())
                val unverifiedCalls = shortener.shortenTraces(tracing.unverified)
                appendLine("Unverified calls for $mock:")
                append(renderer.render(unverifiedCalls))
            }
        }
    }
}
