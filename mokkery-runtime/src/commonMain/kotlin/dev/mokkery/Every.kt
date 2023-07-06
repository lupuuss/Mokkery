@file:Suppress("UNUSED_PARAMETER", "unused")

package dev.mokkery

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.MokkeryPluginNotAppliedException
import dev.mokkery.matcher.ArgMatchersScope

public fun <T> every(
    block: ArgMatchersScope.() -> T
): BlockingAnsweringScope<T> = throw MokkeryPluginNotAppliedException()

public fun <T> everySuspend(
    block: suspend ArgMatchersScope.() -> T
): SuspendAnsweringScope<T> = throw MokkeryPluginNotAppliedException()
