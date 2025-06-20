@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.plugin.core

import dev.mokkery.MokkeryConfig
import dev.mokkery.plugin.ir.locationInFile
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.ir.IrElement


internal inline fun TransformerScope.mokkeryErrorAt(element: IrElement, message: () -> String): Nothing {
    val location = element.locationInFile(currentFile) ?: error("<unknown-location> ${message()}")
    error(
        "file:///${location.path}:${location.line}:${location.column} ${message()}"
    )
}

internal inline fun TransformerScope.mokkeryErrorAt(element: IrElement, message: String): Nothing = mokkeryErrorAt(element) {
    message
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
