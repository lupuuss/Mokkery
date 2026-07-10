package dev.mokkery.plugin.core.ir.transformer

import dev.mokkery.internal.MokkeryConfig
import dev.mokkery.plugin.core.context.configuration
import org.jetbrains.kotlin.cli.reportLog

context(scope: TransformerScope)
inline fun log(message: () -> String) {
    configuration.reportLog("${MokkeryConfig.PLUGIN_ID}: ${message()}")
}

