@file:Suppress("UNUSED_PARAMETER")

package dev.mokkery

import dev.mokkery.internal.MokkeryPluginNotAppliedException

public inline fun <reified T> mock(mode: MockMode = MockMode.Default): T = throw MokkeryPluginNotAppliedException()
