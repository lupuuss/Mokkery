package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.IrMokkeryKind
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.getProperty
import dev.mokkery.plugin.core.mockMode
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.irMokkeryKindValue
import dev.mokkery.plugin.ir.kClassReference
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
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
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getSimpleFunction

fun TransformerScope.buildMockJsFunction(
    expression: IrCall,
    kind: IrMokkeryKind,
    functionClass: IrClass
): IrExpression {
    val anyNType = pluginContext.irBuiltIns.anyNType
    val typeToMock = functionClass.defaultTypeErased
    val returnType = typeToMock.let { it as IrSimpleType }
        .arguments
        .last()
        .typeOrNull
        ?: anyNType
    val transformer = this
    return declarationIrBuilder(expression) {
        irBlock {
            val modeArg = irMockModeArg(transformer, expression, kind)
            val mokkeryScopeCall = irCallMokkeryMockScope(transformer, typeToMock, modeArg, kind)
            val scopeVar = createTmpVariable(mokkeryScopeCall)
            val lambda = irLambda(returnType, typeToMock, currentFile) {
                val irSpyCall = if (kind == IrMokkeryKind.Spy) {
                    irLambdaInvokeSpy(transformer, expression.valueArguments[0]!!, it)
                } else {
                    null
                }
                +irReturn(irInterceptCall(transformer, irGet(scopeVar), it, irSpyCall))
            }
            val lambdaVar = createTmpVariable(lambda)
            +irCallRegisterScope(transformer, irGet(scopeVar), irGet(lambdaVar))
            val block = expression.valueArguments.getOrNull(1)
            if (block != null) {
                +irInvoke(block, false, irGet(lambdaVar))
            }
            +irGet(lambdaVar)
        }
    }
}

private fun IrBuilderWithScope.irMockModeArg(
    transformer: TransformerScope,
    expression: IrCall,
    kind: IrMokkeryKind
): IrExpression {
    val mockModeClass = transformer.getClass(Mokkery.Class.MockMode)
    return when (kind) {
        IrMokkeryKind.Spy -> irGetEnumEntry(mockModeClass, "strict")
        IrMokkeryKind.Mock -> expression
            .valueArguments
            .getOrNull(0)
            ?: irGetEnumEntry(mockModeClass, transformer.mockMode.toString())
    }
}

private fun IrBuilderWithScope.irCallMokkeryMockScope(
    transformer: TransformerScope,
    typeToMock: IrType,
    modeArg: IrExpression,
    kind: IrMokkeryKind,
): IrExpression {
    val mokkeryMockScopeFun = transformer.getFunction(Mokkery.Function.MokkeryMockInstance)
    return irCall(mokkeryMockScopeFun) {
        putValueArgument(0, modeArg)
        putValueArgument(1, irMokkeryKindValue(transformer.getClass(Mokkery.Class.MokkeryKind), kind))
        putValueArgument(2, irString(typeToMock.classFqName!!.asString()))
        putValueArgument(3, kClassReference(typeToMock))
    }
}

private fun IrBuilderWithScope.irCallRegisterScope(
    transformer: TransformerScope,
    mokkeryScope: IrExpression,
    obj: IrExpression
): IrCall {
    val globalContext = transformer.getProperty(Mokkery.Property.GlobalMokkeryContext)
    val instanceLookupProperty = transformer.getProperty(Mokkery.Property.mokkeryInstanceLookup)
    val instanceLookupCall = irCall(instanceLookupProperty.getter!!) {
        extensionReceiver = irCall(globalContext.getter!!)
    }
    val lookUpClass = transformer.getClass(Mokkery.Class.MokkeryInstanceLookup)
    val registerCall = irCall(lookUpClass.getSimpleFunction("register")!!) {
        dispatchReceiver = instanceLookupCall
        putValueArgument(0, obj)
        putValueArgument(1, mokkeryScope)
    }
    return registerCall
}


private fun IrBlockBodyBuilder.irLambdaInvokeSpy(
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
