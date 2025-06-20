package dev.mokkery.debug

import dev.mokkery.interceptor.MokkeryCallListener
import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.suiteName
import dev.mokkery.internal.utils.callToString

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
 * MokkeryCallInterceptor
 *    .beforeAnswering
 *    .register(MokkeryCallLogger())
 * ```
 */
public class MokkeryCallLogger(
    private val lineTransformer: (String) -> String = { it },
    private val loggingFunction: (String) -> Unit = ::println,
) : MokkeryCallListener {

    override fun onIntercept(scope: MokkeryCallScope) {
        scope.suiteName
            ?.let { "[$it] " }
            .orEmpty()
            .plus(callToString(scope.instanceSpec.id, scope.call.function.name, scope.call.args))
            .let(lineTransformer)
            .let(loggingFunction)
    }
}
