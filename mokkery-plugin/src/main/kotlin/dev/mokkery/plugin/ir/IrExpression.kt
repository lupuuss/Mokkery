package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.symbols.IrReturnTargetSymbol
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

fun IrElement.locationInFile(file: IrFile) = CompilerMessageLocation.create(
    path = file.path,
    line = file.fileEntry.getLineNumber(startOffset) + 1,
    column = file.fileEntry.getColumnNumber(startOffset) + 1,
    lineContent = this.render()
)

@Suppress("UNCHECKED_CAST")
fun <T : IrElement> T.removeReturnsTargeting(symbol: IrReturnTargetSymbol): T = transform(
    transformer = object : IrElementTransformerVoid() {
        override fun visitReturn(expression: IrReturn): IrExpression {
            if (expression.returnTargetSymbol != symbol) return super.visitReturn(expression)
            super.visitReturn(expression)
            return expression.value
        }
    },
    data = null
) as T

fun IrCall.transformArguments(transformer: (IrExpression?) -> IrExpression?) {
    val count = arguments.size
    for (i in 0..<count) {
        arguments[i] = transformer(arguments[i])
    }
}
