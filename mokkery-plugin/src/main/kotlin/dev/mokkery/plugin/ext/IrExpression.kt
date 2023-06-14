package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrExpression

fun IrExpression.locationInFile(file: IrFile) = buildString {
    append(file.path)
    append(":")
    append(file.fileEntry.getLineNumber(startOffset) + 1)
    append(":")
    append(file.fileEntry.getColumnNumber(startOffset) + 1)
}
