@file:Suppress("UNUSED_PARAMETER", "unused", "UnusedReceiverParameter")

package dev.mokkery

import dev.mokkery.context.require
import dev.mokkery.internal.annotations.TemplatingLambda
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.callTracing
import dev.mokkery.internal.context.MocksRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.forEachScope
import dev.mokkery.internal.names.aliasTraces
import dev.mokkery.internal.names.withShorterNames
import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.utils.failAssertion
import dev.mokkery.internal.utils.toBeReplacedByCompilerPlugin
import dev.mokkery.templating.MokkeryTemplatingScope
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
    block: @TemplatingLambda MokkeryTemplatingScope.() -> Unit
): Unit = toBeReplacedByCompilerPlugin

/**
 * Just like [verify], but allows suspendable function calls.
 */
public fun verifySuspend(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: @TemplatingLambda suspend MokkeryTemplatingScope.() -> Unit
): Unit = toBeReplacedByCompilerPlugin


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
    block: @TemplatingLambda MokkeryTemplatingScope.() -> Unit
): Unit = toBeReplacedByCompilerPlugin

/**
 * Just like [verify], but allows suspendable function calls.
 *
 * If verify mode is exhaustive, mocks from [MokkerySuiteScope] are also checked.
 */
public fun MokkerySuiteScope.verifySuspend(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: @TemplatingLambda suspend MokkeryTemplatingScope.() -> Unit
): Unit = toBeReplacedByCompilerPlugin

/**
 * Asserts that all given [mocks] have all their registered calls verified with [verify] or [verifySuspend].
 */
public fun verifyNoMoreCalls(vararg mocks: Any) {
    val instances = mocks.map(Any::requireInstanceScope)
    MokkerySuiteScope(MocksRegistry(mocks = instances)).verifyNoMoreCalls()
}

/**
 * Asserts that all mocks from given [MokkerySuiteScope] have no unverified calls.
 */
public fun MokkerySuiteScope.verifyNoMoreCalls() {
    val mocks = mokkeryContext
        .require(MocksRegistry)
        .mocks
        .withShorterNames(tools.namesShortener)
    mocks.forEachScope { mock ->
        val tracing = mock.callTracing
        if (tracing.unverified.isNotEmpty()) {
            failAssertion {
                val renderer = PointListRenderer<CallTrace>()
                val unverifiedCalls = mocks.aliasTraces(tracing.unverified)
                appendLine("Unverified calls for $mock:")
                append(renderer.render(unverifiedCalls))
            }
        }
    }
}
