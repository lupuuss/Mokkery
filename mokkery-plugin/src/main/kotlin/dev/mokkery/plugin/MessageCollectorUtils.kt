package dev.mokkery.plugin

import dev.mokkery.BuildConfig
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal inline fun MessageCollector.info(message: () -> String) {
    report(CompilerMessageSeverity.WARNING, "${BuildConfig.MOKKERY_PLUGIN_ID}: ${message()}")
}
