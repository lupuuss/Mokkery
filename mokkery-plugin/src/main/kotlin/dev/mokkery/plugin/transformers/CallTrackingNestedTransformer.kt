package dev.mokkery.plugin.transformers

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class CallTrackingNestedTransformer(
    private val table: Map<IrClass, IrClass>
) : IrElementTransformerVoid() {

    private val _trackedExpressions = mutableSetOf<IrExpression>()
    val trackedExpressions: Set<IrExpression> = _trackedExpressions

    override fun visitCall(expression: IrCall): IrExpression {
        val dispatchReceiver = expression.dispatchReceiver ?: return super.visitCall(expression)
        if (!table.containsKey(dispatchReceiver.type.getClass())) {
            return super.visitCall(expression)
        }
        _trackedExpressions.add(dispatchReceiver)
        // make return type nullable to avoid runtime checks on non-primitive types (e.g. suspend fun on K/N)
        if (!expression.type.isPrimitiveType(nullable = false)) {
            expression.type = expression.type.makeNullable()
        }
        return super.visitCall(expression)
    }
}
