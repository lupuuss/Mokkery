@file:Suppress("UNUSED_PARAMETER", "unused")

package dev.mokkery

import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.resolveMockInstance
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.interceptor
import dev.mokkery.internal.names.createGroupMockReceiverShortener
import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.utils.failAssertion
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
 * Asserts that calls sequence defined in [block] satisfies given [mode].
 *
 * If verify mode is exhaustive, mocks from [MokkeryTestsScope] are also checked.
 *
 * Each verification is performed only on unverified calls. In result repeated verifications may give different results.
 *
 * Provided [block] **must** be a lambda and all mock calls **must** occur directly inside it. Extracting [block]
 * content to functions is prohibited.
 */
public fun MokkeryTestsScope.verify(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

/**
 * Just like [verify], but allows suspendable function calls.
 *
 * If verify mode is exhaustive, mocks from [MokkeryTestsScope] are also checked.
 */
public fun MokkeryTestsScope.verifySuspend(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: suspend ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

/**
 * Asserts that all given [mocks] have all their registered calls verified with [verify] or [verifySuspend].
 */
public fun verifyNoMoreCalls(vararg mocks: Any) {
    MokkeryTestsScope(MocksRegistry(mocks = mocks.toSet())).verifyNoMoreCalls()
}

/**
 * Asserts that all mocks from given [MokkeryTestsScope] have no unverified calls.
 */
public fun MokkeryTestsScope.verifyNoMoreCalls() {
    val tools = this.tools
    mocks.forEach { mock ->
        val tracing = mock
            .let { tools.resolveMockInstance(it) }
            ?.interceptor
            ?.callTracing ?: throw ObjectNotMockedException(mock)
        if (tracing.unverified.isNotEmpty()) {
            failAssertion {
                val renderer = PointListRenderer<CallTrace>()
                val shortener = tools.createGroupMockReceiverShortener()
                shortener.prepare(tracing.unverified, emptyList())
                val unverifiedCalls = shortener.shortenTraces(tracing.unverified)
                appendLine("Unverified calls for $mock:")
                append(renderer.render(unverifiedCalls))
            }
        }
    }
}
