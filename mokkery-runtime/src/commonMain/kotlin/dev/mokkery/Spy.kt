package dev.mokkery

import dev.mokkery.internal.MokkeryPluginNotAppliedException

@Suppress("UNUSED_PARAMETER")
public inline fun <reified T> spy(obj: T): T = throw MokkeryPluginNotAppliedException()