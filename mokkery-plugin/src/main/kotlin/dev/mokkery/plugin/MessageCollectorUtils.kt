package dev.mokkery.plugin

import dev.mokkery.MokkeryConfig
import dev.mokkery.plugin.ir.locationInFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrExpression

@Suppress("DEPRECATION")
val CompilerConfiguration.messageCollectorCompat: MessageCollector get() = try {
    messageCollector
} catch (e: NoSuchMethodError) {
    get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
}

internal inline fun MessageCollector.log(message: () -> String) {
    report(CompilerMessageSeverity.LOGGING, "${MokkeryConfig.PLUGIN_ID}: ${message()}")
}


internal inline fun MessageCollector.logAt(expression: IrExpression, file: IrFile, message: () -> String) {
    report(
        severity = CompilerMessageSeverity.LOGGING,
        message = "${MokkeryConfig.PLUGIN_ID}: ${message()} Expression at: ${expression.locationInFile(file)}"
    )
}
