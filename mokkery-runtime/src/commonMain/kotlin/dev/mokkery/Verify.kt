@file:Suppress("UNUSED_PARAMETER", "unused")

package dev.mokkery

import dev.mokkery.context.require
import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.resolveScope
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.mokkeryMockInterceptor
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
 * If verify mode is exhaustive, mocks from [MokkerySuiteScope] are also checked.
 *
 * Each verification is performed only on unverified calls. In result repeated verifications may give different results.
 *
 * Provided [block] **must** be a lambda and all mock calls **must** occur directly inside it. Extracting [block]
 * content to functions is prohibited.
 */
public fun MokkerySuiteScope.verify(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

/**
 * Just like [verify], but allows suspendable function calls.
 *
 * If verify mode is exhaustive, mocks from [MokkerySuiteScope] are also checked.
 */
public fun MokkerySuiteScope.verifySuspend(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: suspend ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

/**
 * Asserts that all given [mocks] have all their registered calls verified with [verify] or [verifySuspend].
 */
public fun verifyNoMoreCalls(vararg mocks: Any) {
    val tools = GlobalMokkeryScope.tools
    val instances = mocks.map(tools::resolveScope)
    MokkerySuiteScope(MocksRegistry(mocks = instances)).verifyNoMoreCalls()
}

/**
 * Asserts that all mocks from given [MokkerySuiteScope] have no unverified calls.
 */
public fun MokkerySuiteScope.verifyNoMoreCalls() {
    mokkeryContext
        .require(MocksRegistry)
        .mocks
        .scopes
        .forEach { mock ->
            val tracing = mock
                .mokkeryMockInterceptor
                .callTracing
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
