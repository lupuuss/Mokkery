package dev.mokkery.plugin.core

import dev.mokkery.MokkeryConfig
import dev.mokkery.plugin.ir.locationInFile
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.IrElement


internal inline fun TransformerScope.mokkeryErrorAt(element: IrElement, message: () -> String) {
    messageCollector.report(
        severity = CompilerMessageSeverity.ERROR,
        message = message(),
        location = element.locationInFile(currentFile)
    )
}

internal inline fun TransformerScope.mokkeryLogAt(element: IrElement, message: () -> String) {
    messageCollector.report(
        severity = CompilerMessageSeverity.LOGGING,
        message = "${MokkeryConfig.PLUGIN_ID}: ${message()}",
        location = element.locationInFile(currentFile)
    )
}

internal inline fun TransformerScope.mokkeryLog(message: () -> String) {
    messageCollector.report(CompilerMessageSeverity.LOGGING, "${MokkeryConfig.PLUGIN_ID}: ${message()}")
}
