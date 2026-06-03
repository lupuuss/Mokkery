package dev.mokkery.plugin.core.ir.transformer

import dev.mokkery.internal.MokkeryConfig
import dev.mokkery.plugin.core.ir.messageCollector
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity

context(scope: TransformerScope)
inline fun log(message: () -> String) {
    messageCollector.report(CompilerMessageSeverity.LOGGING, "${MokkeryConfig.PLUGIN_ID}: ${message()}")
}

