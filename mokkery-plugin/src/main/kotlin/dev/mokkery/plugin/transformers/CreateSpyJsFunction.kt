package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ext.defaultTypeErased
import dev.mokkery.plugin.ext.irCall
import dev.mokkery.plugin.ext.irInvoke
import dev.mokkery.plugin.ext.irLambda
import dev.mokkery.plugin.ext.irTryCatchAny
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeOrNull

fun TransformerScope.createSpyJsFunction(expression: IrCall, classToSpy: IrClass): IrExpression {
    val anyNType = pluginContext.irBuiltIns.anyNType
    val typeToSpy = classToSpy.defaultTypeErased
    val returnType = typeToSpy.let { it as IrSimpleType }.arguments.last().typeOrNull ?: anyNType
    return declarationIrBuilder(expression) {
        irBlock {
            val spiedObj = expression.valueArguments[0]!!
            val mokkeryScopeCall = irCall(getFunction(Mokkery.Function.MokkerySpyScope)) {
                putValueArgument(0, irString(typeToSpy.classFqName!!.asString()))
            }
            val scopeVar = createTmpVariable(mokkeryScopeCall)
            val lambda = irLambda(returnType, typeToSpy, currentFile) { lambdaFun ->
                val expr = irIfThenElse(
                    type = returnType,
                    condition = irCallIsTemplatingEnabled(this@createSpyJsFunction, irGet(scopeVar)),
                    thenPart = irInterceptCall(this@createSpyJsFunction, irGet(scopeVar), lambdaFun),
                    elsePart = irBlock {
                        val args = lambdaFun.valueParameters.map { irGet(it) }.toTypedArray()
                        +irTryCatchAny(irInterceptCall(this@createSpyJsFunction, irGet(scopeVar), lambdaFun))
                        +irReturn(irInvoke(spiedObj, lambdaFun.isSuspend, *args))
                    }
                )
                +irReturn(expr)
            }
            val lambdaVar = createTmpVariable(lambda)
            +irCallRegisterScope(this@createSpyJsFunction, irGet(scopeVar), irGet(lambdaVar))
            +irGet(lambdaVar)
        }
    }
}
