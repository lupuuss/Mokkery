package dev.mokkery.plugin.transformers

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class CallTrackingNestedTransformer(
    private val table: Map<IrClass, IrClass>
) : IrElementTransformerVoid() {

    private var firstDeclarationsToSkip = 2
    private val localDeclarations = mutableListOf<IrDeclaration>()
    private val _trackedExpressions = mutableMapOf<IrSymbolOwner, IrExpression>()
    val trackedExpressions: List<IrExpression> get() = _trackedExpressions.values.toList()

    override fun visitCall(expression: IrCall): IrExpression {
        val dispatchReceiver = expression.dispatchReceiver ?: return super.visitCall(expression)
        if (!table.containsKey(dispatchReceiver.type.getClass())) {
            return super.visitCall(expression)
        }
        // make return type nullable to avoid runtime checks on non-primitive types (e.g. suspend fun on K/N)
        if (!expression.type.isPrimitiveType(nullable = false)) {
            expression.type = expression.type.makeNullable()
        }
        return super.visitCall(expression)
    }



    override fun visitDeclarationReference(expression: IrDeclarationReference): IrExpression {
        if (!table.containsKey(expression.type.getClass())) return super.visitDeclarationReference(expression)
        if (localDeclarations.contains(expression.symbol.owner)) return super.visitDeclarationReference(expression)
        _trackedExpressions[expression.symbol.owner] = expression
        return super.visitDeclarationReference(expression)
    }

    override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
        if (firstDeclarationsToSkip > 0) {
            firstDeclarationsToSkip--
            return super.visitDeclaration(declaration)
        }
        localDeclarations.add(declaration)
        return super.visitDeclaration(declaration)
    }
}
