package dev.mokkery.plugin.core

import dev.mokkery.MokkeryConfig
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.messageCollector
import dev.mokkery.plugin.ext.locationInFile
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

internal inline fun TransformerScope.mokkeryErrorAt(call: IrExpression, message: () -> String): Nothing {
    error("${MokkeryConfig.PLUGIN_ID}: ${message()} Failed at: ${call.locationInFile(currentFile)}")
}

internal inline fun TransformerScope.mokkeryLogAt(expression: IrExpression, message: () -> String) {
    messageCollector.report(
        severity = CompilerMessageSeverity.LOGGING,
        message = "${MokkeryConfig.PLUGIN_ID}: ${message()} Expression at: ${expression.locationInFile(currentFile)}"
    )
}

internal inline fun TransformerScope.mokkeryLog(message: () -> String) {
    messageCollector.report(CompilerMessageSeverity.LOGGING, "${MokkeryConfig.PLUGIN_ID}: ${message()}")
}
