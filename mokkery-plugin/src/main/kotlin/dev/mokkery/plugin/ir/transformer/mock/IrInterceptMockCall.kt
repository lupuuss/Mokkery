package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedGetter
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.indexIfParameterOrNull
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.isSuperCallFor
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.core.irCallMapOf
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isSuspend
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.makeTypeParameterSubstitutionMap
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.substitute

context(scope: TransformerScope)
fun IrBlockBodyBuilder.irInterceptMockMemberCall(
    mokkeryKind: IrMokkeryKind,
    function: IrSimpleFunction,
): IrCall = irInterceptMockCall(
    mokkeryKind = mokkeryKind,
    mokkeryInstance = irGet(function.parameters[0]),
    typeParamsContainer = function.parentAsClass,
    function = function
)

context(scope: TransformerScope)
fun IrBlockBodyBuilder.irInterceptMockCall(
    mokkeryKind: IrMokkeryKind,
    mokkeryInstance: IrExpression,
    typeParamsContainer: IrTypeParametersContainer,
    function: IrSimpleFunction,
): IrCall {
    val interceptorProperty = referencedGetter(MokkeryIr.Property.callInterceptor)
    val interceptorClass = interceptorProperty.returnType.classOrFail
    val getSpiedObject = referencedGetter(MokkeryIr.Property.spiedObject)
    val interceptFun = interceptorClass
        .functions
        .first { it.owner.name.asString() == "intercept" && it.owner.isSuspend == function.isSuspend }
    return irCall(interceptFun) {
        arguments[0] = interceptorProperty
            .let(::irCall)
            .apply { arguments[0] = mokkeryInstance }
        val scopeCreationFun = when {
            function.isSuspend -> MokkeryIr.Function.createSuspendCallScope
            else -> MokkeryIr.Function.createBlockingCallScope
        }
        val scopeCreationCall = irCall(referenced(scopeCreationFun)) {
            arguments[0] = mokkeryInstance
            arguments[1] = irString(function.name.asString())
            arguments[2] = kClassWithTypeSubstitution(
                mokkeryInstance = mokkeryInstance,
                typeParamsContainer = typeParamsContainer,
                type = function.returnType
            )
            arguments[3] = irCallArgsList(mokkeryInstance, function, typeParamsContainer)
            arguments[4] = irCallSupersMap(function)
            if (mokkeryKind == IrMokkeryKind.Spy) {
                val spiedObjectGet = irCall(getSpiedObject) { arguments[0] = mokkeryInstance }
                // js function does not have a dispatch parameter
                val spyLambda = if (function.parameters.find { it.kind == IrParameterKind.DispatchReceiver } == null) {
                    irLambdaSpyFunctionCall(spiedObjectGet, function)
                } else {
                    irLambdaSpyMethodCall(spiedObjectGet, function)
                }
                arguments[5] = spyLambda
            }
        }
        arguments[1] = scopeCreationCall
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCallArgsList(
    mokkeryInstance: IrExpression,
    function: IrSimpleFunction,
    paramsContainer: IrTypeParametersContainer
): IrCall {
    val callArgClass = referenced(MokkeryIr.Class.CallArgument)
    val callArgs = function
        .nonDispatchParameters
        .map { param ->
            val constructor = callArgClass.constructors.single { it.parameters.size == 4 }
            irCallConstructor(constructor) {
                arguments[0] = irGet(param)
                arguments[1] = irString(param.name.asString())
                arguments[2] = kClassWithTypeSubstitution(
                    mokkeryInstance = mokkeryInstance,
                    typeParamsContainer = paramsContainer,
                    type = param.type
                )
                arguments[3] = irBoolean(param.isVararg)
            }
        }
    return irCallListOf(callArgClass.defaultType, callArgs)
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCallSupersMap(function: IrSimpleFunction): IrCall? {
    val supers = function.overriddenSymbols
        .filter { it.owner.isSuperCallFor(function) }
        .takeIf { it.isNotEmpty() }
        ?.map { it.owner }
        ?: return null
    val superLambdas = supers.map { superFunction ->
        val kClass = kClassReference(superFunction.parentAsClass.defaultType)
        val lambda = createSuperCallLambda(function, superFunction)
        kClass to lambda
    }
    val builtIns = context.irBuiltIns
    return irCallMapOf(
        pairs = superLambdas,
        keyType = builtIns.kClassClass.starProjectedType,
        valueType = builtIns.functionClass.typeWith(builtIns.anyNType)
    )
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.createSuperCallLambda(
    function: IrSimpleFunction,
    superFunction: IrSimpleFunction
): IrExpression {
    val substitutionMap = makeTypeParameterSubstitutionMap(superFunction, function)
    return irLambdaOf(superFunction.dynamicFunctionType(substitutionMap)) { lambda ->
        +irReturn(
            irDynamicCall(
                function = superFunction,
                dispatchReceiver = irGet(function.parameters[0]),
                argumentsList = irGet(lambda.parameters[0]),
                superQualifierSymbol = superFunction.parentAsClass.symbol,
                substitutionMap = substitutionMap
            )
        )
    }
}

context(scope: TransformerScope)
private fun IrBlockBodyBuilder.irLambdaSpyMethodCall(
    spyObjectDelegate: IrExpression,
    function: IrSimpleFunction,
): IrFunctionExpression = irLambdaOf(function.dynamicFunctionType()) { lambda ->
    val spyFun = function.overriddenSymbols.first().owner
    +irReturn(
        irDynamicCall(
            function = spyFun,
            dispatchReceiver = spyObjectDelegate,
            argumentsList = irGet(lambda.parameters[0]),
            substitutionMap = makeTypeParameterSubstitutionMap(spyFun, function)
        )
    )
}

context(scope: TransformerScope)
private fun IrBlockBodyBuilder.irLambdaSpyFunctionCall(
    delegateLambda: IrExpression,
    function: IrSimpleFunction,
): IrFunctionExpression = irLambdaOf(function.dynamicFunctionType()) { lambda ->
    val args = Array(function.parameters.size) {
        irCall(irBuiltIns.listClass.owner.getSimpleFunction("get")!!) {
            arguments[0] = irGet(lambda.parameters[0])
            arguments[1] = irInt(it)
        }
    }
    +irReturn(irInvoke(function = delegateLambda, isSuspend = lambda.isSuspend, args = args))
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.kClassWithTypeSubstitution(
    mokkeryInstance: IrExpression,
    typeParamsContainer: IrTypeParametersContainer,
    type: IrType
): IrExpression = type
    .indexIfParameterOrNull(typeParamsContainer)
    ?.let { index ->
        irCall(referenced(MokkeryIr.Function.typeArgumentAt)) {
            arguments[0] = mokkeryInstance
            arguments[1] = irInt(index)
        }
    } ?: kClassReference(type.eraseTypeParameters())


context(scope: TransformerScope)
private fun IrFunction.dynamicFunctionType(substitutionMap: Map<IrTypeParameterSymbol, IrType> = emptyMap()): IrType {
    val lambdaClass = if (isSuspend) irBuiltIns.suspendFunctionN(1) else irBuiltIns.functionN(1)
    val lambdaType = lambdaClass.typeWith(
        irBuiltIns.listClass.owner.defaultTypeErased,
        returnType.substitute(substitutionMap)
    )
    return lambdaType
}


context(scope: TransformerScope)
private fun IrBuilder.irDynamicCall(
    function: IrSimpleFunction,
    dispatchReceiver: IrExpression,
    argumentsList: IrExpression,
    substitutionMap: Map<IrTypeParameterSymbol, IrType> = emptyMap(),
    superQualifierSymbol: IrClassSymbol? = null
): IrCall = irCall(
    symbol = function.symbol,
    type = function.returnType.substitute(substitutionMap),
    superQualifierSymbol = superQualifierSymbol,
) {
    arguments[0] = dispatchReceiver
    function.typeParameters.forEachIndexed { i, type -> typeArguments[i] = type.defaultType }
    function.nonDispatchParameters.forEachIndexed { index, irValueParameter ->
        arguments[irValueParameter] = irAs(
            argument = irCall(irBuiltIns.listClass.owner.requireSimpleFunctionOwner("get")) {
                arguments[0] = argumentsList
                arguments[1] = irInt(index)
            },
            type = irValueParameter.type.substitute(substitutionMap)
        )
    }
}
