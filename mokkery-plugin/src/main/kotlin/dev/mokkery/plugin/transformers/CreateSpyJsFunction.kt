package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.kClassReference
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getSimpleFunction

fun TransformerScope.createSpyJsFunction(expression: IrCall, classToSpy: IrClass): IrExpression {
    val anyNType = pluginContext.irBuiltIns.anyNType
    val typeToSpy = classToSpy.defaultTypeErased
    val returnType = typeToSpy.let { it as IrSimpleType }.arguments.last().typeOrNull ?: anyNType
    return declarationIrBuilder(expression) {
        irBlock {
            val spiedObj = expression.valueArguments[0]!!
            val mokkeryScopeCall = irCall(getFunction(Mokkery.Function.MokkerySpyScope)) {
                putValueArgument(0, irString(typeToSpy.classFqName!!.asString()))
                putValueArgument(1, kClassReference(classToSpy.defaultTypeErased))
            }
            val scopeVar = createTmpVariable(mokkeryScopeCall)
            val lambda = irLambda(returnType, typeToSpy, currentFile) { lambdaFun ->
                val spyCall = irInvokeSpyLambda(this@createSpyJsFunction, spiedObj, lambdaFun)
                +irReturn(irInterceptCall(this@createSpyJsFunction, irGet(scopeVar), lambdaFun, spyCall))
            }
            val lambdaVar = createTmpVariable(lambda)
            +irCallRegisterScope(this@createSpyJsFunction, irGet(scopeVar), irGet(lambdaVar))
            +irGet(lambdaVar)
        }
    }
}

private fun IrBlockBodyBuilder.irInvokeSpyLambda(
    transformer: TransformerScope,
    delegateLambda: IrExpression,
    function: IrSimpleFunction,
): IrFunctionExpression {
    val pluginContext = transformer.pluginContext
    val lambdaType = pluginContext
        .irBuiltIns
        .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
        .typeWith(pluginContext.irBuiltIns.listClass.owner.defaultTypeErased, function.returnType)
    return irLambda(
        returnType = function.returnType,
        lambdaType = lambdaType,
        parent = parent,
    ) { lambda ->
        val args = Array(function.valueParameters.size) {
            irCall(context.irBuiltIns.listClass.owner.getSimpleFunction("get")!!) {
                dispatchReceiver = irGet(lambda.valueParameters[0])
                putValueArgument(0, irInt(it))
            }
        }
        +irReturn(irInvoke(function = delegateLambda, isSuspend = lambda.isSuspend, args = args))
    }
}