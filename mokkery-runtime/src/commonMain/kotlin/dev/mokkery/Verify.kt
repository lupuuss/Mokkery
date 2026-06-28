@file:Suppress("UNUSED_PARAMETER", "unused", "UnusedReceiverParameter")

package dev.mokkery

import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.mokkeryIntrinsic
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
public fun verifyNoMoreCalls(vararg mocks: Any): Unit = mokkeryIntrinsic

/**
 * Asserts that all mocks from given [MokkerySuiteScope] have no unverified calls.
 */
public fun MokkerySuiteScope.verifyNoMoreCalls(): Unit = mokkeryIntrinsic
