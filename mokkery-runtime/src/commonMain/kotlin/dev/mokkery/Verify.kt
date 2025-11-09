@file:Suppress("UNUSED_PARAMETER", "unused", "UnusedReceiverParameter")

package dev.mokkery

import dev.mokkery.context.require
import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.tracing.callTracing
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.forEachScope
import dev.mokkery.internal.instanceId
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.utils.mokkeryIntrinsic
import dev.mokkery.internal.verify.render.NoMoreCallsErrorRenderer
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
    block: @Templating MokkeryTemplatingScope.() -> Unit
): Unit = mokkeryIntrinsic

/**
 * Just like [verify], but allows suspendable function calls.
 */
public fun verifySuspend(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: @Templating suspend MokkeryTemplatingScope.() -> Unit
): Unit = mokkeryIntrinsic


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
    block: @Templating MokkeryTemplatingScope.() -> Unit
): Unit = mokkeryIntrinsic

/**
 * Just like [verify], but allows suspendable function calls.
 *
 * If verify mode is exhaustive, mocks from [MokkerySuiteScope] are also checked.
 */
public fun MokkerySuiteScope.verifySuspend(
    mode: VerifyMode = MokkeryCompilerDefaults.verifyMode,
    block: @Templating suspend MokkeryTemplatingScope.() -> Unit
): Unit = mokkeryIntrinsic

/**
 * Asserts that all given [mocks] have all their registered calls verified with [verify] or [verifySuspend].
 */
public fun verifyNoMoreCalls(vararg mocks: Any) {
    val instances = mocks.map(Any::requireInstanceScope)
    MokkerySuiteScope(MokkeryInstancesRegistry(instances = instances)).verifyNoMoreCalls()
}

/**
 * Asserts that all mocks from given [MokkerySuiteScope] have no unverified calls.
 */
public fun MokkerySuiteScope.verifyNoMoreCalls() {
    val mocks = mokkeryContext
        .require(MokkeryInstancesRegistry)
        .collection
    mocks.forEachScope { mock ->
        val tracing = mock.callTracing
        val unverified = tracing.unverified
        if (unverified.isNotEmpty()) {
            val message = NoMoreCallsErrorRenderer
                .factory(tools.namesShortener, mocks)
                .create()
                .render(mock.instanceId to unverified)
            throw AssertionError(message)
        }
    }
}
