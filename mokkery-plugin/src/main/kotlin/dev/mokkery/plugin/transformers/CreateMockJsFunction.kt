package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.mockMode
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ext.defaultTypeErased
import dev.mokkery.plugin.ext.irCall
import dev.mokkery.plugin.ext.irGetEnumEntry
import dev.mokkery.plugin.ext.irInvoke
import dev.mokkery.plugin.ext.irLambda
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeOrNull

fun TransformerScope.createMockJsFunction(expression: IrCall, classToMock: IrClass): IrExpression {
    val anyNType = pluginContext.irBuiltIns.anyNType
    val typeToMock = classToMock.defaultTypeErased
    val returnType = typeToMock.let { it as IrSimpleType }.arguments.last().typeOrNull ?: anyNType
    val mockModeClass = getClass(Mokkery.Class.MockMode)
    val mokkeryMockScopeFun = getFunction(Mokkery.Function.MokkeryMockScope)
    return declarationIrBuilder(expression) {
        irBlock {
            val modeArg = expression.valueArguments
                .getOrNull(0)
                ?: irGetEnumEntry(mockModeClass, mockMode.toString())
            val mokkeryScopeCall = irCall(mokkeryMockScopeFun) {
                putValueArgument(0, modeArg)
                putValueArgument(1, irString(typeToMock.classFqName!!.asString()))
            }
            val scopeVar = createTmpVariable(mokkeryScopeCall)
            val lambda = irLambda(returnType, typeToMock, currentFile) {
                +irReturn(irInterceptCall(this@createMockJsFunction, irGet(scopeVar), it))
            }
            val lambdaVar = createTmpVariable(lambda)
            +irCallRegisterScope(this@createMockJsFunction, irGet(scopeVar), irGet(lambdaVar))
            val block = expression.valueArguments.getOrNull(1)
            if (block != null) {
                +irInvoke(block, false, irGet(lambdaVar))
            }
            +irGet(lambdaVar)
        }
    }
}
