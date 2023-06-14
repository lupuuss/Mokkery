@file:Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER", "unused")

package dev.mokkery

import dev.mokkery.answer.MockAnswerScope
import dev.mokkery.answer.MockSuspendAnswerScope
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.matcher.ArgMatchersScope

public fun <T> every(
    block: ArgMatchersScope.() -> T
): MockAnswerScope<T> = throw MokkeryPluginNotAppliedException()

public suspend fun <T> everySuspend(
    block: suspend ArgMatchersScope.() -> T
): MockSuspendAnswerScope<T> = throw MokkeryPluginNotAppliedException()
