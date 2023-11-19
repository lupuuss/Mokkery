package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.util.render

fun IrElement.locationInFile(file: IrFile) = CompilerMessageLocation.create(
    path = file.path,
    line = file.fileEntry.getLineNumber(startOffset) + 1,
    column = file.fileEntry.getColumnNumber(startOffset) + 1,
    lineContent = this.render()
)
