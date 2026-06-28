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
 *
 * @param mode determines how strict the verification should be. If not provided, default value is used.
 */
public fun verify(
    mode: VerifyMode? = null,
    block: @Templating MokkeryTemplatingScope.() -> Unit
): Unit = mokkeryIntrinsic

/**
 * Just like [verify], but allows suspendable function calls.
 *
 * @param mode determines how strict the verification should be. If not provided, default value is used.
 */
public fun verifySuspend(
    mode: VerifyMode? = null,
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
 *
 * @param mode determines how strict the verification should be. If not provided, default value is used.
 */
public fun MokkerySuiteScope.verify(
    mode: VerifyMode? = null,
    block: @Templating MokkeryTemplatingScope.() -> Unit
): Unit = mokkeryIntrinsic

/**
 * Just like [verify], but allows suspendable function calls.
 *
 * If verify mode is exhaustive, mocks from [MokkerySuiteScope] are also checked.
 *
 * @param mode determines how strict the verification should be. If not provided, default value is used.
 */
public fun MokkerySuiteScope.verifySuspend(
    mode: VerifyMode? = null,
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
