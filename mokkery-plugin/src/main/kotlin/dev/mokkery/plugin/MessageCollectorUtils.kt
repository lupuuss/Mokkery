package dev.mokkery.plugin

import dev.mokkery.MokkeryConfig
import dev.mokkery.plugin.ext.locationInFile
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrExpression

internal inline fun MessageCollector.log(message: () -> String) {
    report(CompilerMessageSeverity.LOGGING, "${MokkeryConfig.PLUGIN_ID}: ${message()}")
}


internal inline fun MessageCollector.logAt(expression: IrExpression, file: IrFile, message: () -> String) {
    report(
        severity = CompilerMessageSeverity.LOGGING,
        message = "${MokkeryConfig.PLUGIN_ID}: ${message()} Expression at: ${expression.locationInFile(file)}"
    )
}

internal inline fun MessageCollector.warningAt(expression: IrExpression, file: IrFile, message: () -> String) {
    report(
        severity = CompilerMessageSeverity.WARNING,
        message = "${MokkeryConfig.PLUGIN_ID}: ${message()} Expression at: ${expression.locationInFile(file)}"
    )
}
