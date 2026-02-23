package dev.mokkery.plugin.ir.transformer.core

import dev.mokkery.MokkeryConfig
import dev.mokkery.plugin.ir.messageCollector
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity

context(scope: TransformerScope)
inline fun info(message: () -> String) {
    messageCollector.report(CompilerMessageSeverity.INFO, "${MokkeryConfig.PLUGIN_ID}: ${message()}")
}

