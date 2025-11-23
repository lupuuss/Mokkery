package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrReturnTarget
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.symbols.IrReturnTargetSymbol
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid

fun IrElement.locationInFile(file: IrFile) = CompilerMessageLocation.create(
    path = file.path,
    line = file.fileEntry.getLineNumber(startOffset) + 1,
    column = file.fileEntry.getColumnNumber(startOffset) + 1,
    lineContent = this.render()
)

fun IrMemberAccessExpression<*>.transformArguments(transformer: (IrExpression?) -> IrExpression?) {
    val count = arguments.size
    for (i in 0..<count) {
        arguments[i] = transformer(arguments[i])
    }
}

inline fun IrElement.forEachReturnTargeting(symbol: IrReturnTargetSymbol, crossinline block: (IrReturn) -> Unit) {
    transformChildren(
        transformer = object : IrElementTransformerVoid() {
            override fun visitReturn(expression: IrReturn): IrExpression {
                if (expression.returnTargetSymbol == symbol) {
                    block(expression)
                }
                return super.visitReturn(expression)
            }
        },
        data = null
    )
}

fun IrReturnTarget.collectReturns(): List<IrReturn> = buildList {
    forEachReturnTargeting(symbol) { add(it) }
}
