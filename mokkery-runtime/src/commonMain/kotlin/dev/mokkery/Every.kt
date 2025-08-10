@file:Suppress("UNUSED_PARAMETER", "unused")

package dev.mokkery

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.annotations.TemplatingLambda
import dev.mokkery.internal.utils.toBeReplacedByCompilerPlugin
import dev.mokkery.templating.MokkeryTemplatingScope

/**
 *
 * Starts defining an answer for a **single** function call specified by [block]. Returned scope allows defining answer.
 *
 * Provided [block] **must** be a lambda and single mock call **must** occur directly inside it. Extracting [block]
 * content to functions is prohibited.
 */
public fun <T> every(
    block: @TemplatingLambda MokkeryTemplatingScope.() -> T
): BlockingAnsweringScope<T> = toBeReplacedByCompilerPlugin

/**
 * Just like [every], but allows suspendable function call.
 */
public fun <T> everySuspend(
    block: @TemplatingLambda suspend MokkeryTemplatingScope.() -> T
): SuspendAnsweringScope<T> = toBeReplacedByCompilerPlugin
