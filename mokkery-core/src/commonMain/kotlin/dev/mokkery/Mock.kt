package dev.mokkery

public inline fun <reified T> mock(): T = throw MokkeryPluginNotAppliedException()
