package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.util.render

fun IrElement.locationInFile(file: IrFile) = CompilerMessageLocation.create(
    path = file.path,
    line = file.fileEntry.getLineNumber(startOffset) + 1,
    column = file.fileEntry.getColumnNumber(startOffset) + 1,
    lineContent = this.render()
)

fun IrFunctionAccessExpression.transformArguments(transformer: (IrExpression?) -> IrExpression?) {
    val count = arguments.size
    for (i in 0..<count) {
        arguments[i] = transformer(arguments[i])
    }
}
