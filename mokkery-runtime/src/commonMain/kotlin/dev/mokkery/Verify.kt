@file:Suppress( "UNUSED_PARAMETER", "unused")
package dev.mokkery

import dev.mokkery.internal.MokkeryMockInstance
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.context.GlobalMokkeryContext
import dev.mokkery.internal.mokkeryInstanceLookup
import dev.mokkery.internal.utils.failAssertion
import dev.mokkery.internal.interceptor
import dev.mokkery.internal.names.createGroupMockReceiverShortener
import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.tools
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
            .let { GlobalMokkeryContext.mokkeryInstanceLookup.resolve(it) as? MokkeryMockInstance }
            ?.interceptor
            ?.callTracing ?: throw ObjectNotMockedException(mock)
        if (tracing.unverified.isNotEmpty()) {
            failAssertion {
                val renderer = PointListRenderer<CallTrace>()
                val shortener = GlobalMokkeryContext.tools.createGroupMockReceiverShortener()
                shortener.prepare(tracing.unverified, emptyList())
                val unverifiedCalls = shortener.shortenTraces(tracing.unverified)
                appendLine("Unverified calls for $mock:")
                append(renderer.render(unverifiedCalls))
            }
        }
    }
}
