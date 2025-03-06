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
import dev.mokkery.plugin.ir.indexIfParameterOrNull
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irGetEnumEntry
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.irMokkeryKindValue
import dev.mokkery.plugin.ir.kClassReference
import org.jetbrains.kotlin.backend.jvm.ir.eraseTypeParameters
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.getSimpleFunction

fun TransformerScope.buildMockJsFunction(
    expression: IrCall,
    kind: IrMokkeryKind
): IrExpression {
    val typeToMock = expression.type
    val typeArguments = typeToMock.let { it as IrSimpleType }
        .arguments
        .map { it.typeOrFail.eraseTypeParameters() }
    val returnType = typeArguments.last()
    val transformer = this
    return declarationIrBuilder(expression) {
        irBlock {
            val modeArg = irMockModeArg(transformer, expression, kind)
            val parentScope = expression.extensionReceiver ?: irGetObject(transformer.getClass(Mokkery.Class.GlobalMokkeryScope).symbol)
            val mokkeryInstanceCall = irCallMokkeryMockInstance(transformer, parentScope, typeToMock, modeArg, kind)
            val instanceVar = createTmpVariable(mokkeryInstanceCall)
            val lambda = irLambda(returnType, typeToMock, currentFile) {
                val irSpyCall = if (kind == IrMokkeryKind.Spy) {
                    irLambdaInvokeSpy(transformer, expression.valueArguments[0]!!, it)
                } else {
                    null
                }
                val parentClass = typeToMock.classOrFail.owner
                val mapTypeToClass: IrBuilderWithScope.(IrType) -> IrExpression = {
                    val index = it.indexIfParameterOrNull(parentClass)
                    if (index != null) kClassReference(typeArguments[index])
                    else kClassReference(it)
                }
                +irReturn(irInterceptCall(transformer, irGet(instanceVar), it, mapTypeToClass, irSpyCall))
            }
            val lambdaVar = createTmpVariable(lambda)
            +irCallRegisterScope(transformer, irGet(instanceVar), irGet(lambdaVar))
            +irCall(transformer.getFunction(Mokkery.Function.invokeMockInstantiationCallbacks)) {
                this.extensionReceiver = irGet(instanceVar)
            }
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

private fun IrBuilderWithScope.irCallMokkeryMockInstance(
    transformer: TransformerScope,
    parentScope: IrExpression,
    typeToMock: IrType,
    modeArg: IrExpression,
    kind: IrMokkeryKind,
): IrExpression {
    val instanceScopeFun = transformer.getFunction(Mokkery.Function.MokkeryInstanceScope)
    return irCall(instanceScopeFun) {
        putValueArgument(0, parentScope)
        putValueArgument(1, modeArg)
        putValueArgument(2, irMokkeryKindValue(transformer.getClass(Mokkery.Class.MokkeryKind), kind))
        putValueArgument(3, irString(typeToMock.classFqName!!.asString()))
        putValueArgument(4, kClassReference(typeToMock))
    }
}

private fun IrBuilderWithScope.irCallRegisterScope(
    transformer: TransformerScope,
    mokkeryInstance: IrExpression,
    obj: IrExpression
): IrCall {
    val instanceLookupCall = irCall(transformer.getProperty(Mokkery.Property.GlobalMokkeryScopeLookup).getter!!)
    val lookUpClass = transformer.getClass(Mokkery.Class.MokkeryScopeLookup)
    val registerCall = irCall(lookUpClass.getSimpleFunction("registerScope")!!) {
        dispatchReceiver = instanceLookupCall
        putValueArgument(0, obj)
        putValueArgument(1, mokkeryInstance)
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
