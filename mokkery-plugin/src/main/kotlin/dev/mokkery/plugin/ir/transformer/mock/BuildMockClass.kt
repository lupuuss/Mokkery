package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.annotationSelector
import dev.mokkery.plugin.core.context.configuration
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.irFactory
import dev.mokkery.plugin.core.ir.pluginContext
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.addToCurrentFile
import dev.mokkery.plugin.core.ir.transformer.declarationIrBuilder
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedGetterSymbol
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.addOverridingProperty
import dev.mokkery.plugin.ir.annotations.toFilter
import dev.mokkery.plugin.ir.computeSignature
import dev.mokkery.plugin.ir.createParametersMapTo
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.overridableProperties
import dev.mokkery.plugin.ir.overridePropertyBackingField
import dev.mokkery.plugin.ir.requirePropertyOwner
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.core.recordSuperTypesLookUp
import dev.mokkery.plugin.ir.transformer.mock.stubs.irDelegatingConstructorWithStubs
import dev.mokkery.plugin.ir.typeWith
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.memoryOptimizedMap

context(scope: TransformerScope)
fun buildMockClass(
    name: Name,
    mokkeryKind: IrMokkeryKind,
    classToMock: IrClass,
): IrClass {
    val instanceScopeClass = referenced(MokkeryIr.Class.mokkeryInstanceScope(mokkeryKind))
    val mockedClass = irFactory.buildClass { this.name = name }
    mockedClass.addToCurrentFile()
    mockedClass.copyTypeParametersFrom(classToMock)
    val typedClassToMock = classToMock.symbol.typeWithParameters(mockedClass.typeParameters)
    mockedClass.superTypes = listOfNotNull(
        typedClassToMock,
        instanceScopeClass.defaultType,
        if (classToMock.isInterface) irBuiltIns.anyType else null
    )
    mockedClass.createThisReceiverParameter()
    mockedClass.origin = MokkeryIr.Origin
    val mutableScopeClass = referenced(MokkeryIr.Class.MutableMokkeryInstanceScope)
    mockedClass.overridePropertyBackingField(pluginContext, mutableScopeClass.requirePropertyOwner("mokkeryContext"))
    mockedClass.overrideToString()
    mockedClass.addMockClassConstructor(
        typeName = classToMock.kotlinFqName.asString(),
        mokkeryKind = mokkeryKind,
        classesToIntercept = listOf(classToMock),
    )
    val functions = mockedClass.overrideInterceptedFunctions(listOf(classToMock)) { function ->
        +irReturn(irInterceptMockMemberCall(function))
    }
    mockedClass.addInstanceContracts(mokkeryKind, listOf(classToMock), functions)
    recordSuperTypesLookUp(listOf(classToMock))
    return mockedClass
}

context(scope: TransformerScope)
fun buildManyMockClass(name: Name, classesToMock: List<IrClass>): IrClass {
    val manyMocksMarkerClass = referenced(MokkeryIr.Class.mockMany(classesToMock.size))
    val mokkeryInstanceClass = referenced(MokkeryIr.Class.MutableMokkeryMockScope)
    val mockedClass = irFactory.buildClass { this.name = name }
    mockedClass.addToCurrentFile()
    classesToMock.forEach(mockedClass::copyTypeParametersFrom)
    mockedClass.createThisReceiverParameter()
    mockedClass.origin = MokkeryIr.Origin
    val parameterMap = classesToMock.createParametersMapTo(mockedClass)
    val mockedTypes = classesToMock.typeWith(parameterMap)
    val manyMocksMarkerType = manyMocksMarkerClass.symbol.typeWith(mockedTypes)
    mockedClass.superTypes = mockedTypes + listOfNotNull(
        mokkeryInstanceClass.defaultType,
        if (classesToMock.all(IrClass::isInterface)) irBuiltIns.anyType else null,
        manyMocksMarkerType
    )
    val mutableScopeClass = referenced(MokkeryIr.Class.MutableMokkeryInstanceScope)
    mockedClass.overridePropertyBackingField(pluginContext, mutableScopeClass.requirePropertyOwner("mokkeryContext"))
    mockedClass.overrideToString()
    mockedClass.addMockClassConstructor(
        mokkeryKind = IrMokkeryKind.Mock,
        typeName = mockManyTypeName(manyMocksMarkerClass, classesToMock),
        classesToIntercept = classesToMock,
    )
    val functions = mockedClass.overrideInterceptedFunctions(classesToMock) { function ->
        +irReturn(irInterceptMockMemberCall(function))
    }
    mockedClass.addInstanceContracts(IrMokkeryKind.Mock, classesToMock, functions)
    recordSuperTypesLookUp(classesToMock)
    return mockedClass
}

context(scope: TransformerScope)
private fun IrClass.overrideToString() {
    val toString = irBuiltIns.anyClass.owner.requireSimpleFunctionOwner("toString")
    addOverridingMethod(pluginContext, toString) {
        val instanceIdString = referencedGetterSymbol(MokkeryIr.Property.instanceIdString)
        +irReturn(
            irCall(instanceIdString) {
                arguments[0] = irGet(it.parameters[0])
            }
        )
    }
}

context(scope: TransformerScope)
private fun IrClass.overrideInterceptedFunctions(
    classesToIntercept: List<IrClass>,
    body: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
): List<IrSimpleFunction> {
    val annotationFilter = configuration.annotationSelector.toFilter()
    val parameterMap = classesToIntercept.createParametersMapTo(this)
    val functions = classesToIntercept
        .flatMap { it.overridableFunctions }
        .groupBy { it.computeSignature() }
        .map { (_, functions) ->
            addOverridingMethod(
                context = pluginContext,
                functions = functions,
                parameterMap = parameterMap,
                annotationFilter = annotationFilter,
                block = { body(it) }
            )
        }
    val accessorFunctions = classesToIntercept
        .flatMap { it.overridableProperties }
        .groupBy { it.computeSignature() }
        .flatMap { (_, properties) ->
            addOverridingProperty(
                context = pluginContext,
                properties = properties,
                parameterMap = parameterMap,
                annotationFilter = annotationFilter,
                getterBlock = { body(it) },
                setterBlock = { body(it) }
            ).let { listOfNotNull(it.getter, it.setter) }
        }
    return functions + accessorFunctions
}

private fun mockManyTypeName(klass: IrClass, types: List<IrClass>): String {
    return "${klass.kotlinFqName.asString()}<${types.joinToString { it.kotlinFqName.asString() }}>"
}

context(scope: TransformerScope)
private fun IrClass.addMockClassConstructor(
    mokkeryKind: IrMokkeryKind,
    typeName: String,
    classesToIntercept: List<IrClass>,
) {
    val mokkeryScopeClass = referenced(MokkeryIr.Class.MokkeryScope)
    val mockModeClass = referenced(MokkeryIr.Class.MockMode)
    val receiverParam = thisReceiver!!
    addConstructor {
        isPrimary = true
    }.apply {
        addValueParameter("parent", mokkeryScopeClass.defaultType)
        addValueParameter("mode", mockModeClass.defaultType.makeNullable())
        addValueParameter("block", irBuiltIns.functionN(2).defaultTypeErased.makeNullable())
        val spyParam = when (mokkeryKind) {
            IrMokkeryKind.Spy -> addSpyParameter(classesToIntercept)
            IrMokkeryKind.Mock -> null
        }
        val kClassType = irBuiltIns.kClassClass.starProjectedType
        val typeParameters = classesToIntercept
            .memoryOptimizedMap { it.typeParameters }
            .let { classParams ->
                var index = 0
                classParams.memoryOptimizedMap {
                    it.memoryOptimizedMap {
                        addValueParameter("type${index++}", kClassType)
                    }
                }
            }
        body = symbol.declarationIrBuilder.irBlockBody {
            +irDelegatingConstructorWithStubs(
                irClass = classesToIntercept.firstOrNull { it.isClass },
                subClass = this@addMockClassConstructor
            )
            +irCall(referenced(MokkeryIr.Function.setupMokkeryInstanceForCommon)) {
                arguments[0] = irGet(receiverParam)
                arguments[1] = irGet(parameters[0])
                arguments[2] = irString(typeName)
                arguments[3] = irCallListOf(
                    type = kClassType,
                    elements = classesToIntercept.memoryOptimizedMap { kClassReference(it.defaultTypeErased) }
                )
                arguments[4] = irCallListOf(
                    type = irBuiltIns.listClass.typeWith(kClassType),
                    elements = typeParameters.memoryOptimizedMap { params ->
                        irCallListOf(
                            type = kClassType,
                            elements = params.memoryOptimizedMap { irGet(it) }
                        )
                    }
                )
                arguments[5] = irGet(parameters[1])
                arguments[6] = spyParam?.let(::irGet) ?: irNull()
                arguments[7] = irGet(parameters[2])
            }
        }
    }
}

private fun IrConstructor.addSpyParameter(classesToIntercept: List<IrClass>): IrValueParameter {
    val classToSpy = classesToIntercept.singleOrNull() ?: error("Spy is not supported for intercepting multiple types!")
    return addValueParameter("obj", classToSpy.symbol.typeWithParameters(parentAsClass.typeParameters))
}
