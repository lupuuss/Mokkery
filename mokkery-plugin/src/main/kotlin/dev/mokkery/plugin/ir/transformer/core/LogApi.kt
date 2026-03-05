package dev.mokkery.plugin.ir.transformer.core

import dev.mokkery.internal.MokkeryConfig
import dev.mokkery.plugin.ir.messageCollector
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity

context(scope: TransformerScope)
inline fun log(message: () -> String) {
    messageCollector.report(CompilerMessageSeverity.LOGGING, "${MokkeryConfig.PLUGIN_ID}: ${message()}")
}

