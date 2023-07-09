package dev.mokkery.plugin

import dev.mokkery.MokkeryConfig
import dev.mokkery.plugin.ext.locationInFile
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrExpression

internal inline fun mokkeryError(message: () -> String): Nothing {
    error("${MokkeryConfig.PLUGIN_ID}: ${message()}")
}

internal inline fun IrExpression.mokkeryError(file: IrFile, message: () -> String): Nothing {
    error("${MokkeryConfig.PLUGIN_ID}: ${message()} Failed at: ${locationInFile(file)}")
}
