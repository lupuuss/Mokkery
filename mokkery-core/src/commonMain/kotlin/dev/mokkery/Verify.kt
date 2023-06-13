@file:Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER", "unused")
package dev.mokkery

import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.internal.MokkeryScope
import dev.mokkery.internal.ObjectNotMockedMockedExcpetion
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyMode.Companion.default

public fun verify(
    mode: VerifyMode = default,
    block: ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

public suspend fun verifySuspend(
    mode: VerifyMode = default,
    block: suspend ArgMatchersScope.() -> Unit
): Unit = throw MokkeryPluginNotAppliedException()

public fun verifyNoMoreCalls(vararg mocks: Any) {
    mocks.forEach {
        if (it !is MokkeryScope) throw ObjectNotMockedMockedExcpetion(it)
        if (it.mokkery.unverifiedTraces.isNotEmpty()) {
            throw AssertionError("Unverified calls for $it: \n\t${it.mokkery.unverifiedTraces.joinToString("\n\t")}")
        }
    }
}
