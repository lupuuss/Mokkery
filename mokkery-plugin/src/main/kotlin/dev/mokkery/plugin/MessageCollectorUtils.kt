package dev.mokkery.plugin

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

fun MessageCollector.info(message: String) = report(CompilerMessageSeverity.INFO, message)
