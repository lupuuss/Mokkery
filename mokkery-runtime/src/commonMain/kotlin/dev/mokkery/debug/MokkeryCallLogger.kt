package dev.mokkery.debug

import dev.mokkery.MokkeryCallScope
import dev.mokkery.interceptor.MokkeryCallListener
import dev.mokkery.internal.context.suiteName
import dev.mokkery.internal.rendering.callScopeRenderer
import dev.mokkery.internal.rendering.withRenderingScope

/**
 * Logs each mock call with [loggingFunction]. By default, the [loggingFunction] is [println].
 *
 * Log line can be changed using [lineTransformer].
 *
 * To enable the logger, it has to be registered in a hook.
 *
 * Example:
 *
 * ```kotlin
 * // Globally
 * MokkeryScope
 *    .global
 *    .callHooks
 *    .beforeAnswering
 *    .register(MokkeryCallLogger())
 *
 * // Per mock
 * MokkeryScope
 *    .from(mock)
 *    .callHooks
 *    .beforeAnswering
 *    .register(MokkeryCallLogger())
 * ```
 *
 * @see dev.mokkery.interceptor.MokkeryCallHooks
 */
public class MokkeryCallLogger(
    private val lineTransformer: (String) -> String = { it },
    private val loggingFunction: (String) -> Unit = ::println,
) : MokkeryCallListener {


    override fun onIntercept(scope: MokkeryCallScope) {
        scope.withRenderingScope {
            scope.suiteName
                ?.let { "[$it] " }
                .orEmpty()
                .plus(callScopeRenderer.render(scope))
                .let(lineTransformer)
                .let(loggingFunction)
        }
    }
}
