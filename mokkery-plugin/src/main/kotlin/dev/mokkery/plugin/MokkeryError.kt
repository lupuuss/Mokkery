package dev.mokkery.plugin

import dev.mokkery.BuildConfig

fun mokkeryError(message: String): Nothing = error("${BuildConfig.MOKKERY_PLUGIN_ID}: $message")
