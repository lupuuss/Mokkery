package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.annotationSelector
import dev.mokkery.plugin.context.configuration
import dev.mokkery.plugin.ir.IrMokkeryKind
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.addOverridingProperty
import dev.mokkery.plugin.ir.annotations.toFilter
import dev.mokkery.plugin.ir.computeSignature
import dev.mokkery.plugin.ir.createParametersMapTo
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.irBuiltIns
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irFactory
import dev.mokkery.plugin.ir.irInvokeIfNotNull
import dev.mokkery.plugin.ir.irSetPropertyField
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.overridableProperties
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import dev.mokkery.plugin.ir.overridePropertyBackingField
import dev.mokkery.plugin.ir.pluginContext
import dev.mokkery.plugin.ir.requirePropertyOwner
import dev.mokkery.plugin.ir.transformer.core.TransformerScope
import dev.mokkery.plugin.ir.transformer.core.addToCurrentFile
import dev.mokkery.plugin.ir.transformer.core.declarationIrBuilder
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import dev.mokkery.plugin.ir.transformer.core.referenced
import dev.mokkery.plugin.ir.transformer.mock.stubs.irDelegatingConstructorWithStubs
import dev.mokkery.plugin.ir.typeWith
import dev.mokkery.plugin.randomString
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
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
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
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.memoryOptimizedMap

context(scope: TransformerScope)
fun buildMockClass(
    mokkeryKind: IrMokkeryKind,
    classToMock: IrClass,
): IrClass {
    val instanceScopeClass = referenced(MokkeryIr.Class.MokkeryInstanceScope)
    val mockedClass = irFactory.buildClass { name = classToMock.name.createUniqueMockName(mokkeryKind.name) }
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
    mockedClass.addMockClassConstructor(
        typeName = classToMock.kotlinFqName.asString(),
        mokkeryKind = mokkeryKind,
        scopeInstanceClass = instanceScopeClass,
        classesToIntercept = listOf(classToMock),
    )
    val annotationFilter = configuration
        .annotationSelector
        .toFilter()
    mockedClass.overrideAllOverridableFunctions(pluginContext, classToMock, annotationFilter) {
        mockMemberFunctionBody(mokkeryKind, it)
    }
    mockedClass.overrideAllOverridableProperties(
        context = pluginContext,
        superClass = classToMock,
        annotationFilter = annotationFilter,
        getterBlock = { mockMemberFunctionBody(mokkeryKind, it) },
        setterBlock = { mockMemberFunctionBody(mokkeryKind, it) }
    )
    mockedClass.metadata = classToMock.metadata
    return mockedClass
}

context(scope: TransformerScope)
fun buildManyMockClass(classesToMock: List<IrClass>): IrClass {
    val manyMocksMarkerClass = referenced(MokkeryIr.Class.mockMany(classesToMock.size))
    val mokkeryInstanceClass = referenced(MokkeryIr.Class.MokkeryInstanceScope)
    val mockedClass = irFactory.buildClass {
        name = manyMocksMarkerClass.kotlinFqName.createUniqueManyMockName()
    }
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
    mockedClass.addMockClassConstructor(
        scopeInstanceClass = mokkeryInstanceClass,
        mokkeryKind = IrMokkeryKind.Mock,
        typeName = mockManyTypeName(manyMocksMarkerClass, classesToMock),
        classesToIntercept = classesToMock,
    )
    val annotationFilter = configuration.annotationSelector.toFilter()
    classesToMock.flatMap { it.overridableFunctions }
        .groupBy(IrDeclaration::computeSignature)
        .map { (_, functions) ->
            mockedClass.addOverridingMethod(
                context = pluginContext,
                functions = functions,
                parameterMap = parameterMap,
                annotationFilter = annotationFilter
            ) {
                mockMemberFunctionBody(IrMokkeryKind.Mock, it)
            }
        }
    classesToMock.flatMap { it.overridableProperties }
        .groupBy(IrDeclaration::computeSignature)
        .map { (_, properties) ->
            mockedClass.addOverridingProperty(
                context = pluginContext,
                properties = properties,
                parameterMap = parameterMap,
                annotationFilter = annotationFilter,
                getterBlock = { mockMemberFunctionBody(IrMokkeryKind.Mock, it) },
                setterBlock = { mockMemberFunctionBody(IrMokkeryKind.Mock, it) }
            )
        }
    return mockedClass
}

private fun mockManyTypeName(klass: IrClass, types: List<IrClass>): String {
    return "${klass.kotlinFqName.asString()}<${types.joinToString { it.kotlinFqName.asString() }}>"
}

context(transformer: TransformerScope)
private fun IrBlockBodyBuilder.mockMemberFunctionBody(
    mokkeryKind: IrMokkeryKind,
    function: IrSimpleFunction,
) {
    +irReturn(irInterceptMockMemberCall(mokkeryKind, function))
}

context(scope: TransformerScope)
private fun IrClass.addMockClassConstructor(
    scopeInstanceClass: IrClass,
    mokkeryKind: IrMokkeryKind,
    typeName: String,
    classesToIntercept: List<IrClass>,
) {
    val mokkeryScopeClass = referenced(MokkeryIr.Class.MokkeryScope)
    val mockModeClass = referenced(MokkeryIr.Class.MockMode)
    val invokeInstantiationCallbacksFun = referenced(MokkeryIr.Function.invokeInstantiationListener)
    val contextProperty = overridePropertyBackingField(pluginContext, scopeInstanceClass.requirePropertyOwner("mokkeryContext"))
    addConstructor {
        isPrimary = true
    }.apply {
        addValueParameter("parent", mokkeryScopeClass.defaultType)
        addValueParameter("mode", mockModeClass.defaultType.makeNullable())
        addValueParameter("block", irBuiltIns.functionN(1).defaultTypeErased.makeNullable())
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
            +irDelegatingConstructorWithStubs(classesToIntercept.firstOrNull { it.isClass })
            +irSetPropertyField(
                thisParam = thisReceiver!!,
                property = contextProperty,
                value = irCall(referenced(MokkeryIr.Function.createInstanceContext)) {
                    arguments[0] = irGet(parameters[0])
                    arguments[1] = irString(typeName)
                    arguments[2] = irCallListOf(
                        type = kClassType,
                        elements = classesToIntercept.memoryOptimizedMap { kClassReference(it.defaultTypeErased) }
                    )
                    arguments[3] = irCallListOf(
                        type = irBuiltIns.listClass.typeWith(kClassType),
                        elements = typeParameters.memoryOptimizedMap { params ->
                            irCallListOf(
                                type = kClassType,
                                elements = params.memoryOptimizedMap { irGet(it) }
                            )
                        }
                    )
                    arguments[4] = irGet(thisReceiver!!)
                    arguments[5] = irGet(parameters[1])
                    arguments[6] = spyParam?.let(::irGet) ?: irNull()
                    arguments[7] = buildDefaultsExtractorFactoryIfRequired(
                        className = this@addMockClassConstructor.name,
                        classesToIntercept = classesToIntercept,
                        bodyBuilder = this@irBlockBody
                    )
                }
            )
            +irCall(invokeInstantiationCallbacksFun) {
                arguments[0] = irGet(thisReceiver!!)
                arguments[1] = irGet(thisReceiver!!)
            }
            +irInvokeIfNotNull(irGet(parameters[2]), false, irGet(thisReceiver!!))
        }
    }
    addOverridingMethod(pluginContext, irBuiltIns.memberToString.owner) {
        +irReturn(irCall(referenced(MokkeryIr.Property.instanceIdString).getter!!.symbol) {
            arguments[0] = irGet(it.parameters[0])
        })
    }
}

private fun IrConstructor.addSpyParameter(classesToIntercept: List<IrClass>): IrValueParameter {
    val classToSpy = classesToIntercept.singleOrNull() ?: error("Spy is not supported for intercepting multiple types!")
    return addValueParameter("obj", classToSpy.symbol.typeWithParameters(parentAsClass.typeParameters))
}

private fun Name.createUniqueMockName(type: String) = asString()
    .plus($$"$$$type$$${randomString()}")
    .let(Name::identifier)

private fun FqName.createUniqueManyMockName() = shortName()
    .asString()
    .plus($$"$$${randomString()}")
    .let(Name::identifier)
