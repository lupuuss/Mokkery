package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.pluginContext
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedGetter
import dev.mokkery.plugin.core.ir.transformer.referencedPrimaryConstructor
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.addOverridingProperty
import dev.mokkery.plugin.ir.copyNonDispatchParametersWithoutDefaultsFrom
import dev.mokkery.plugin.ir.requirePropertyOwner
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.erasedTypeArguments
import dev.mokkery.plugin.ir.erasedUpperBound
import dev.mokkery.plugin.ir.hasDefaultParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irThrow
import dev.mokkery.plugin.ir.isSuperCallFor
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.mokkeryFunctionId
import dev.mokkery.plugin.ir.mokkeryFunctionIds
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.plugin.ir.transformer.core.irCallListGet
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.mock.stubs.irDelegatingConstructorWithStubs
import dev.mokkery.plugin.ir.typeSubstitutionForSuperClass
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irBranch
import org.jetbrains.kotlin.ir.builders.irElseBranch
import org.jetbrains.kotlin.ir.builders.irEquals
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irLong
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irWhen
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.createDispatchReceiverParameterWithClassParent
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fields
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.name.Name

context(scope: TransformerScope)
fun IrClass.addInstanceContracts(
    mokkeryKind: IrMokkeryKind,
    classesToIntercept: List<IrClass>,
    functions: List<IrSimpleFunction>
) {
    val supers = functions.map { function ->
        function.overriddenSymbols
            .map { it.owner }
            .filter { it.isSuperCallFor(this) }
    }
    if (supers.any { it.isNotEmpty() }) {
        addSuperCallsContract(functions, supers)
    }
    if (mokkeryKind == IrMokkeryKind.Spy) {
        addSpyCallsContract(functions)
    }
    addCoreContract(classesToIntercept, functions)
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
        val functionIdParam = addValueParameter("functionId", irBuiltIns.longType)
        body = symbol.declarationIrBuilder.irBlockBody {
            +irDelegatingConstructorWithStubs(
                irClass = classesToIntercept.firstOrNull { it.isClass },
                subClass = this@addMockClassConstructorForDefaults
            )
            +irCall(referenced(MokkeryIr.Function.setupMokkeryInstanceForDefaults)) {
                arguments[0] = irGet(receiverParam)
                arguments[1] = irGet(ownerParam)
                arguments[2] = irGet(functionIdParam)
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
    addFunctionIdDispatcher(
        contractFunction = dispatcherClass.requireSimpleFunctionOwner("mokkerySuperTypes"),
        branches = supers.mapIndexedNotNull { index, superFunctions ->
            if (superFunctions.isEmpty()) return@mapIndexedNotNull null
            FunctionIdBranch(id = functions[index].mokkeryFunctionId, weight = superFunctions.size + 1) {
                irCallListOf(
                    type = kClassType,
                    elements = superFunctions.map { kClassReference(it.parentAsClass.defaultTypeErased) }
                )
            }
        },
        elseResult = { irCallListOf(kClassType, emptyList()) }
    )
    listOf(false, true).forEach { isSuspend ->
        val name = if (isSuspend) "mokkerySuperCallSuspend" else "mokkerySuperCall"
        addFunctionIdDispatcher(
            contractFunction = dispatcherClass.requireSimpleFunctionOwner(name),
            branches = functions.mapIndexedNotNull { index, func ->
                val superFunctions = supers[index]
                if (superFunctions.isEmpty() || func.isSuspend != isSuspend) return@mapIndexedNotNull null
                FunctionIdBranch(id = func.mokkeryFunctionId, weight = func.dispatchWeight * superFunctions.size) { superCallFunc ->
                    val calls = superFunctions.map { superFunction ->
                        irDynamicCall(
                            target = superFunction,
                            mockedClass = this@addSuperCallsContract,
                            dispatchReceiver = irGet(superCallFunc.parameters[0]),
                            argsParam = superCallFunc.parameters[3],
                            superQualifierSymbol = superFunction.parentAsClass.symbol
                        )
                    }
                    when (calls.size) {
                        1 -> calls.single()
                        else -> irWhenInt(
                            type = irBuiltIns.anyNType,
                            subjectParam = superCallFunc.parameters[2],
                            branches = calls.mapIndexed { index, call -> index to call },
                            elseResult = { irThrowMissingDispatcherCall() }
                        )
                    }
                }
            },
            elseResult = { irThrowMissingDispatcherCall() }
        )
    }
}

context(scope: TransformerScope)
private fun IrClass.addSpyCallsContract(functions: List<IrSimpleFunction>) {
    val dispatcherClass = referenced(MokkeryIr.Class.SpyCallsContract)
    superTypes += dispatcherClass.defaultType
    val spiedObjectGetter = referencedGetter(MokkeryIr.Property.spiedObject)
    listOf(false, true).forEach { isSuspend ->
        val name = if (isSuspend) "mokkerySpyCallSuspend" else "mokkerySpyCall"
        addFunctionIdDispatcher(
            contractFunction = dispatcherClass.requireSimpleFunctionOwner(name),
            branches = functions.mapNotNull { func ->
                if (func.isSuspend != isSuspend) return@mapNotNull null
                FunctionIdBranch(id = func.mokkeryFunctionId, weight = func.dispatchWeight) { contractFunc ->
                    irDynamicCall(
                        target = func.overriddenSymbols.first().owner,
                        mockedClass = this@addSpyCallsContract,
                        dispatchReceiver = irCall(spiedObjectGetter) {
                            arguments[0] = irGet(contractFunc.parameters[0])
                        },
                        argsParam = contractFunc.parameters[2],
                        superQualifierSymbol = null
                    )
                }
            },
            elseResult = { irThrowMissingDispatcherCall() }
        )
    }
}

context(scope: TransformerScope)
private fun IrClass.addCoreContract(
    classesToIntercept: List<IrClass>,
    functions: List<IrSimpleFunction>,
) {
    val contractClass = referenced(MokkeryIr.Class.CoreContract)
    superTypes += contractClass.defaultType
    val kClassType = irBuiltIns.kClassClass.starProjectedType
    addOverridingProperty(
        context = pluginContext,
        property = contractClass.requirePropertyOwner("mokkeryInterceptedTypes"),
        getterBlock = {
            +irReturn(
                irCallListOf(
                    type = kClassType,
                    elements = classesToIntercept.memoryOptimizedMap { kClassReference(it.defaultTypeErased) }
                )
            )
        },
        setterBlock = { }
    )
    val typeArgumentsField = this.fields.find { it.name.asString() == "_mokkeryTypeArguments" }
    if (typeArgumentsField != null) {
        addOverridingProperty(
            context = pluginContext,
            property = contractClass.requirePropertyOwner("mokkeryTypeArguments"),
            getterBlock = { getter -> +irReturn(irGetField(irGet(getter.parameters[0]), typeArgumentsField)) },
            setterBlock = { }
        )
    }
    addFunctionIdDispatcher(
        contractFunction = contractClass.requireSimpleFunctionOwner("mokkeryFunction"),
        branches = functions.map { func ->
            FunctionIdBranch(id = func.mokkeryFunctionId, weight = func.dispatchWeight) { contractFunc ->
                irCallCreateFunction(
                    mokkeryInstance = { irGet(contractFunc.parameters[0]) },
                    typeParamsContainer = this@addCoreContract,
                    function = func,
                    functionId = irGet(contractFunc.parameters[1]),
                )
            }
        },
        elseResult = { irNull() }
    )
    val aliases = functions.flatMap { member ->
        val pivot = member.mokkeryFunctionId
        member.mokkeryFunctionIds
            .filter { it != pivot }
            .map { it to pivot }
    }
    if (aliases.isEmpty()) return
    addFunctionIdDispatcher(
        contractFunction = contractClass.requireSimpleFunctionOwner("mokkeryNormalizeId"),
        branches = aliases.map { (alias, pivot) -> FunctionIdBranch(id = alias, weight = 1) { irLong(pivot) } },
        elseResult = { irGet(it.parameters[1]) }
    )
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irThrowMissingDispatcherCall(): IrExpression = irThrow(
    irCallConstructor(referencedPrimaryConstructor(MokkeryIr.Class.MissingDispatcherCall))
)

private fun IrClass.dispatchSubstitutionFor(
    target: IrSimpleFunction
): Map<IrTypeParameterSymbol, IrType> = typeSubstitutionForSuperClass(target.parentAsClass)
    .orEmpty()
    .plus(target.typeParameters.associate { it.symbol to it.erasedUpperBound })

context(scope: TransformerScope)
private fun IrBuilderWithScope.irDynamicCall(
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

private class FunctionIdBranch(
    val id: Long,
    val weight: Int,
    val result: IrBuilderWithScope.(IrSimpleFunction) -> IrExpression,
)

private const val MAX_DISPATCHER_CHUNK_WEIGHT = 350

private val IrSimpleFunction.dispatchWeight get() = nonDispatchParameters.size + 2

context(scope: TransformerScope)
private fun IrClass.addFunctionIdDispatcher(
    contractFunction: IrSimpleFunction,
    branches: List<FunctionIdBranch>,
    elseResult: IrBuilderWithScope.(IrSimpleFunction) -> IrExpression,
) {
    val chunks = branches
        .groupBy { it.id.toInt() }
        .toSortedMap()
        .values
        .chunkedByWeight()
    if (chunks.size <= 1) {
        addOverridingMethod(pluginContext, contractFunction) { irReturnByFunctionId(it, branches, elseResult) }
        return
    }
    val chunkFunctions = chunks.mapIndexed { index, chunk ->
        addDispatcherChunk(contractFunction, index, chunk, elseResult)
    }
    val lessOrEqual = irBuiltIns.lessOrEqualFunByOperandType.getValue(irBuiltIns.intClass)
    addOverridingMethod(pluginContext, contractFunction) { function ->
        val idToCheckInt = createTmpVariable(irCallToInt(irGet(function.parameters[1])))
        +irReturn(
            irWhen(
                type = contractFunction.returnType,
                branches = chunks.dropLast(1).mapIndexed { index, chunk ->
                    irBranch(
                        condition = irCall(lessOrEqual) {
                            arguments[0] = irGet(idToCheckInt)
                            arguments[1] = irInt(chunk.last().id.toInt())
                        },
                        result = irCallDispatcherChunk(chunkFunctions[index], function)
                    )
                } + irElseBranch(irCallDispatcherChunk(chunkFunctions.last(), function))
            )
        )
    }
}

private fun Collection<List<FunctionIdBranch>>.chunkedByWeight(): List<List<FunctionIdBranch>> {
    val chunks = mutableListOf<MutableList<FunctionIdBranch>>()
    var weight = 0
    forEach { group ->
        val groupWeight = group.sumOf(FunctionIdBranch::weight)
        if (chunks.isEmpty() || weight + groupWeight > MAX_DISPATCHER_CHUNK_WEIGHT) {
            chunks += mutableListOf<FunctionIdBranch>()
            weight = 0
        }
        chunks.last() += group
        weight += groupWeight
    }
    return chunks
}

context(scope: TransformerScope)
private fun IrClass.addDispatcherChunk(
    contractFunction: IrSimpleFunction,
    index: Int,
    branches: List<FunctionIdBranch>,
    elseResult: IrBuilderWithScope.(IrSimpleFunction) -> IrExpression,
): IrSimpleFunction = addFunction {
    name = Name.identifier("${contractFunction.name}Chunk$index")
    returnType = contractFunction.returnType
    visibility = DescriptorVisibilities.PRIVATE
    modality = Modality.FINAL
    isSuspend = contractFunction.isSuspend
}.apply {
    parameters = listOf(createDispatchReceiverParameterWithClassParent())
    copyNonDispatchParametersWithoutDefaultsFrom(contractFunction)
    body = symbol.declarationIrBuilder.irBlockBody { irReturnByFunctionId(this@apply, branches, elseResult) }
}

private fun IrBuilderWithScope.irCallDispatcherChunk(
    chunk: IrSimpleFunction,
    function: IrSimpleFunction,
): IrExpression = irCall(chunk.symbol) {
    function.parameters.forEachIndexed { index, param -> arguments[index] = irGet(param) }
}

context(scope: TransformerScope)
private fun IrBlockBodyBuilder.irReturnByFunctionId(
    function: IrSimpleFunction,
    branches: List<FunctionIdBranch>,
    elseResult: IrBuilderWithScope.(IrSimpleFunction) -> IrExpression,
) {
    val subject = function.parameters[1]
    if (branches.isNotEmpty()) {
        val idToCheckInt = createTmpVariable(irCallToInt(irGet(subject)))
        +irWhen(
            type = irBuiltIns.unitType,
            branches = branches
                .groupBy { it.id.toInt() }
                .map { (lowerId, sharedLowerId) ->
                    irBranch(
                        condition = irEquals(irGet(idToCheckInt), irInt(lowerId)),
                        result = irWhen(
                            type = irBuiltIns.unitType,
                            branches = sharedLowerId.map { branch ->
                                irBranch(
                                    condition = irEquals(irGet(subject), irLong(branch.id)),
                                    result = irReturn(branch.result(this, function))
                                )
                            }
                        )
                    )
                }
        )
    }
    +irReturn(elseResult(function))
}

private fun IrBuilderWithScope.irWhenInt(
    type: IrType,
    subjectParam: IrValueDeclaration,
    branches: List<Pair<Int, IrExpression>>,
    elseResult: () -> IrExpression,
): IrExpression = irEqualityWhen(type, branches, elseResult) { irEquals(irGet(subjectParam), irInt(it)) }

private fun <T> IrBuilderWithScope.irEqualityWhen(
    type: IrType,
    branches: List<Pair<T, IrExpression>>,
    elseResult: () -> IrExpression,
    equals: (T) -> IrExpression,
): IrExpression {
    if (branches.isEmpty()) return elseResult()
    return irWhen(
        type = type,
        branches = branches
            .map { (key, result) -> irBranch(equals(key), result) }
            .plus(irElseBranch(elseResult()))
    )
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCallToInt(value: IrExpression): IrExpression = irCall(
    irBuiltIns.longClass.getSimpleFunction("toInt")!!
) {
    arguments[0] = value
}
