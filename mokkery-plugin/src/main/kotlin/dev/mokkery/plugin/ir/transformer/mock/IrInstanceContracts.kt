package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.pluginContext
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedDefaultType
import dev.mokkery.plugin.core.ir.transformer.referencedGetter
import dev.mokkery.plugin.core.ir.transformer.referencedPrimaryConstructor
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.erasedTypeArguments
import dev.mokkery.plugin.ir.erasedUpperBound
import dev.mokkery.plugin.ir.hasDefaultParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irThrow
import dev.mokkery.plugin.ir.isSuperCallFor
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.plugin.ir.transformer.core.irCallListGet
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.mock.stubs.irDelegatingConstructorWithStubs
import dev.mokkery.plugin.ir.typeSubstitutionForSuperClass
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irBranch
import org.jetbrains.kotlin.ir.builders.irElseBranch
import org.jetbrains.kotlin.ir.builders.irEquals
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irWhen
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.substitute

context(scope: TransformerScope)
fun IrClass.addInstanceContracts(
    mokkeryKind: IrMokkeryKind,
    classesToIntercept: List<IrClass>,
    functions: List<IrSimpleFunction>
) {
    val supers = functions.map { member ->
        member.overriddenSymbols
            .map { it.owner }
            .filter { it.isSuperCallFor(this) }
    }
    if (supers.any { it.isNotEmpty() }) {
        addSuperCallsContract(functions, supers)
    }
    if (mokkeryKind == IrMokkeryKind.Spy) {
        addSpyCallsContract(functions)
    }
    val hasDefaults = classesToIntercept.any { it.overridableFunctions.any(IrSimpleFunction::hasDefaultParameters) }
    if (hasDefaults) {
        val ctor = addMockClassConstructorForDefaults(classesToIntercept)
        addDefaultsContract(ctor)
    }
}

context(scope: TransformerScope)
private fun IrClass.addDefaultsContract(constructor: IrConstructor) {
    val factoryClass = referenced(MokkeryIr.Class.DefaultsContract)
    superTypes += factoryClass.defaultType
    val typeArguments = erasedTypeArguments
    addOverridingMethod(pluginContext, factoryClass.requireSimpleFunctionOwner("mokkeryCreateExtractor")) { function ->
        +irReturn(
            irCallConstructor(constructor, typeArguments) {
                arguments[0] = irGet(function.parameters[0])
                arguments[1] = irGet(function.parameters[1])
                arguments[2] = irGet(function.parameters[2])
            }
        )
    }
}

context(scope: TransformerScope)
private fun IrClass.addMockClassConstructorForDefaults(
    classesToIntercept: List<IrClass>,
): IrConstructor {
    val receiverParam = thisReceiver!!
    return addConstructor().apply {
        val ownerParam = addValueParameter("owner", irBuiltIns.anyType)
        val functionNameParam = addValueParameter("functionName", irBuiltIns.stringType)
        val parameterListType = irBuiltIns
            .listClass
            .typeWith(referencedDefaultType(MokkeryIr.Class.FunctionParameter))
        val parametersParam = addValueParameter("parameters", parameterListType)
        body = symbol.declarationIrBuilder.irBlockBody {
            +irDelegatingConstructorWithStubs(
                irClass = classesToIntercept.firstOrNull { it.isClass },
                subClass = this@addMockClassConstructorForDefaults
            )
            +irCall(referenced(MokkeryIr.Function.setupMokkeryInstanceForDefaults)) {
                arguments[0] = irGet(receiverParam)
                arguments[1] = irGet(ownerParam)
                arguments[2] = irGet(functionNameParam)
                arguments[3] = irGet(parametersParam)
            }
        }
    }
}

context(scope: TransformerScope)
private fun IrClass.addSuperCallsContract(
    functions: List<IrSimpleFunction>,
    supers: List<List<IrSimpleFunction>>,
) {
    val dispatcherClass = referenced(MokkeryIr.Class.SuperCallsContract)
    superTypes += dispatcherClass.defaultType
    val kClassType = irBuiltIns.kClassClass.starProjectedType
    addOverridingMethod(pluginContext, dispatcherClass.requireSimpleFunctionOwner("mokkerySuperTypes")) { dispatcher ->
        +irReturn(
            irFunctionIdWhen(
                type = dispatcher.returnType,
                memberId = dispatcher.parameters[1],
                branches = supers.mapIndexedNotNull { id, superFunctions ->
                    if (superFunctions.isEmpty()) return@mapIndexedNotNull null
                    id to irCallListOf(
                        type = kClassType,
                        elements = superFunctions.map { kClassReference(it.parentAsClass.defaultTypeErased) }
                    )
                },
                elseResult = irCallListOf(kClassType, emptyList())
            )
        )
    }
    listOf(false, true).forEach { isSuspend ->
        val name = if (isSuspend) "mokkerySuperCallSuspend" else "mokkerySuperCall"
        addOverridingMethod(pluginContext, dispatcherClass.requireSimpleFunctionOwner(name)) { dispatcher ->
            val superIndexParam = dispatcher.parameters[2]
            val argsParam = dispatcher.parameters[3]
            +irReturn(
                irFunctionIdWhen(
                    type = irBuiltIns.anyNType,
                    memberId = dispatcher.parameters[1],
                    branches = functions.mapIndexedNotNull { id, member ->
                        val superFunctions = supers[id]
                        if (superFunctions.isEmpty() || member.isSuspend != isSuspend) return@mapIndexedNotNull null
                        val calls = superFunctions.map { superFunction ->
                            irDispatchedCall(
                                target = superFunction,
                                mockedClass = this@addSuperCallsContract,
                                dispatchReceiver = irGet(dispatcher.parameters[0]),
                                argsParam = argsParam,
                                superQualifierSymbol = superFunction.parentAsClass.symbol
                            )
                        }
                        id to when (calls.size) {
                            1 -> calls.single()
                            else -> irFunctionIdWhen(
                                type = irBuiltIns.anyNType,
                                memberId = superIndexParam,
                                branches = calls.mapIndexed { index, call -> index to call },
                                elseResult = irThrowMissingDispatcherCall()
                            )
                        }
                    },
                    elseResult = irThrowMissingDispatcherCall()
                )
            )
        }
    }
}

context(scope: TransformerScope)
private fun IrClass.addSpyCallsContract(members: List<IrSimpleFunction>) {
    val dispatcherClass = referenced(MokkeryIr.Class.SpyCallsContract)
    superTypes += dispatcherClass.defaultType
    val spiedObjectGetter = referencedGetter(MokkeryIr.Property.spiedObject)
    listOf(false, true).forEach { isSuspend ->
        val name = if (isSuspend) "mokkerySpyCallSuspend" else "mokkerySpyCall"
        addOverridingMethod(pluginContext, dispatcherClass.requireSimpleFunctionOwner(name)) { dispatcher ->
            val argsParam = dispatcher.parameters[2]
            +irReturn(
                irFunctionIdWhen(
                    type = irBuiltIns.anyNType,
                    memberId = dispatcher.parameters[1],
                    branches = members.mapIndexedNotNull { id, member ->
                        if (member.isSuspend != isSuspend) return@mapIndexedNotNull null
                        id to irDispatchedCall(
                            target = member.overriddenSymbols.first().owner,
                            mockedClass = this@addSpyCallsContract,
                            dispatchReceiver = irCall(spiedObjectGetter) {
                                arguments[0] = irGet(dispatcher.parameters[0])
                            },
                            argsParam = argsParam,
                            superQualifierSymbol = null
                        )
                    },
                    elseResult = irThrowMissingDispatcherCall()
                )
            )
        }
    }
}

private fun IrBuilderWithScope.irFunctionIdWhen(
    type: IrType,
    memberId: IrValueParameter,
    branches: List<Pair<Int, IrExpression>>,
    elseResult: IrExpression,
): IrExpression {
    if (branches.isEmpty()) return elseResult
    return irWhen(
        type = type,
        branches = branches
            .map { (id, result) -> irBranch(irEquals(irGet(memberId), irInt(id)), result) }
            .plus(irElseBranch(elseResult))
    )
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irThrowMissingDispatcherCall(): IrExpression = irThrow(
    irCallConstructor(referencedPrimaryConstructor(MokkeryIr.Class.MissingDispatcherCall))
)

context(scope: TransformerScope)
private fun IrBuilderWithScope.irDispatchedCall(
    target: IrSimpleFunction,
    mockedClass: IrClass,
    dispatchReceiver: IrExpression,
    argsParam: IrValueParameter,
    superQualifierSymbol: IrClassSymbol?,
): IrExpression {
    val substitutionMap = mockedClass.dispatchSubstitutionFor(target)
    return irCall(
        symbol = target.symbol,
        type = target.returnType.substitute(substitutionMap),
        superQualifierSymbol = superQualifierSymbol,
    ) {
        arguments[0] = dispatchReceiver
        target.typeParameters.forEachIndexed { index, param ->
            typeArguments[index] = substitutionMap.getValue(param.symbol)
        }
        target.nonDispatchParameters.forEachIndexed { index, param ->
            arguments[param] = irAs(
                argument = irCallListGet(irGet(argsParam), index),
                type = param.type.substitute(substitutionMap)
            )
        }
    }
}

private fun IrClass.dispatchSubstitutionFor(
    target: IrSimpleFunction
): Map<IrTypeParameterSymbol, IrType> = typeSubstitutionForSuperClass(target.parentAsClass)
    .orEmpty()
    .plus(target.typeParameters.associate { it.symbol to it.erasedUpperBound })
