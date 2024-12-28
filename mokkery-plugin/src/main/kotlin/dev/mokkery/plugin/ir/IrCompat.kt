package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import java.lang.reflect.Parameter


fun IrBuilderWithScope.irCallCompat(
    callee: IrSimpleFunctionSymbol,
    type: IrType,
    valueArgumentsCount: Int = callee.owner.valueParameters.size,
    typeArgumentsCount: Int = callee.owner.typeParameters.size,
    origin: IrStatementOrigin? = null,
): IrCall {
    return try {
        irCall(
            callee = callee,
            type = type,
            typeArgumentsCount = typeArgumentsCount,
            origin = origin,
        )
    } catch (_: NoSuchMethodError) {
        val parameterTypes = listOf(
            IrBuilderWithScope::class.java, // extension receiver
            IrSimpleFunctionSymbol::class.java, // callee
            IrType::class.java, // type
            Int::class.java, // valueArgumentsCount
            Int::class.java, // typeArgumentsCount
            IrStatementOrigin::class.java, // origin
        )
        javaClass
            .classLoader
            .loadClass("org.jetbrains.kotlin.ir.builders.ExpressionHelpersKt")
            .methods
            .single { it.name == "irCall" && it.parameters.map(Parameter::getType) == parameterTypes }
            .invoke(
                null, // static invocation
                this, // extension receiver
                callee, // param: callee
                type, // param: type
                valueArgumentsCount, // param: valueArgumentsCount
                typeArgumentsCount, // param: typeArgumentsCount
                origin, // param: origin
            ) as IrCall
    }
}
