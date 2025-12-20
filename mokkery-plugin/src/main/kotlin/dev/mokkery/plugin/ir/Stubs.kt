package dev.mokkery.plugin.ir

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.stubs.Stub
import dev.mokkery.plugin.stubs.StubStrategyScope
import dev.mokkery.plugin.stubs.with
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.hasDefaultValue

fun IrBuilder.irCallConstructorWithStubs(
    constructorWithStubs: Pair<IrConstructor, List<Stub>>,
    typeArguments: List<IrType> = emptyList(),
    block: IrConstructorCall.() -> Unit = { },
): IrConstructorCall {
    val (constructor, stubs) = constructorWithStubs
    return irCallConstructor(constructor, typeArguments) {
        stubs.forEachIndexed { i, stub ->
            val params = constructor.parameters
            if (!params[i].hasDefaultValue()) {
                arguments[i] = stub.expression
            }
        }
        block()
    }
}

fun IrBuilder.irDelegatingConstructorWithStubs(constructorWithStubs: Pair<IrConstructor, List<Stub>>): IrDelegatingConstructorCall {
    val (constructor, stubs) = constructorWithStubs
    return irDelegatingConstructorCall(constructor).apply {
        val params = constructor.parameters
        stubs.forEachIndexed { i, stub ->
            if (!params[i].hasDefaultValue()) {
                arguments[i] = stub.expression
            }
        }
    }
}

context(scope: StubStrategyScope)
fun IrBlockBodyBuilder.stubFunctionBody(func: IrSimpleFunction) {
    context(scope.with(this)) {
        val value = scope.strategy.provide(func.returnType)
        if (value == null) {
            +irCallError(
                "Mokkery stub value could not be provided for type `${func.returnType.dumpKotlinLike()}`!"
            )
        } else {
            +irReturn(value.expression)
        }
    }
}


context(scope: StubStrategyScope)
private fun IrBlockBodyBuilder.irCallError(
    message: String
): IrCall = irCall(Mokkery.Function.mokkeryRuntimeError.resolve(scope.plugin)) {
    arguments[0] = irString(message)
}
