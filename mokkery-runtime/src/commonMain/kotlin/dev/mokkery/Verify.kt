@file:Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER", "unused")
package dev.mokkery

import dev.mokkery.internal.MokkeryInterceptorScope
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.internal.MokkerySpy
import dev.mokkery.internal.ObjectNotSpiedException
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyMode.Companion.default

public fun verify(
    mode: VerifyMode = default,
    block: ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

public fun verifySuspend(
    mode: VerifyMode = default,
    block: suspend ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

public fun verifyNoMoreCalls(vararg mocks: Any) {
    mocks.forEach { mock ->
        val tracing = mock
            .let { it as? MokkeryInterceptorScope }
            ?.interceptor
            ?.let { it as? MokkerySpy }
            ?.callTracing ?: throw ObjectNotSpiedException(mock)
        if (tracing.unverified.isNotEmpty()) {
            throw AssertionError("Unverified calls for $mock: \n\t${tracing.unverified.joinToString("\n\t")}")
        }
    }
}
