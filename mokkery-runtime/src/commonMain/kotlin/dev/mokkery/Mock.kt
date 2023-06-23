@file:Suppress("UNUSED_PARAMETER")

package dev.mokkery

import dev.mokkery.internal.MokkeryPluginNotAppliedException

public inline fun <reified T : Any> mock(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: T.() -> Unit = { }
): T = throw MokkeryPluginNotAppliedException()
