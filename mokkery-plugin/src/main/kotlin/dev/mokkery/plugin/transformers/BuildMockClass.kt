package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.IrMokkeryKind
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.getProperty
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.addOverridingProperty
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.getProperty
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irDelegatingDefaultConstructorOrAny
import dev.mokkery.plugin.ir.irInvokeIfNotNull
import dev.mokkery.plugin.ir.irMokkeryKindValue
import dev.mokkery.plugin.ir.irSetPropertyField
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.overridableProperties
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import dev.mokkery.plugin.ir.overridePropertyBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerIr.signatureString
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
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.createParameterDeclarations
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.memoryOptimizedFlatMap
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.memoryOptimizedMapIndexed
import org.jetbrains.kotlin.utils.memoryOptimizedZip
import java.util.*

fun TransformerScope.buildMockClass(
    mokkeryKind: IrMokkeryKind,
    classToMock: IrClass,
): IrClass {
    val instanceScopeClass = getClass(Mokkery.Class.MokkeryInstanceScope)
    val mockedClass = pluginContext
        .irFactory
        .buildClass { name = classToMock.name.createUniqueMockName(mokkeryKind.name) }
    mockedClass.copyTypeParametersFrom(classToMock)
    val typedClassToMock = classToMock.symbol.typeWithParameters(mockedClass.typeParameters)
    mockedClass.superTypes = listOfNotNull(
        typedClassToMock,
        instanceScopeClass.defaultType,
        if (classToMock.isInterface) pluginContext.irBuiltIns.anyType else null
    )
    mockedClass.createParameterDeclarations()
    mockedClass.origin = Mokkery.Origin
    mockedClass.addMockClassConstructor(
        transformer = this,
        typeName = classToMock.kotlinFqName.asString(),
        mokkeryKind = mokkeryKind,
        scopeInstanceClass = instanceScopeClass,
        classesToIntercept = listOf(classToMock),
    )
    mockedClass.overrideAllOverridableFunctions(pluginContext, classToMock) {
        mockBody(this@buildMockClass, mokkeryKind, it)
    }
    mockedClass.overrideAllOverridableProperties(
        context = pluginContext,
        superClass = classToMock,
        getterBlock = { mockBody(this@buildMockClass, mokkeryKind, it) },
        setterBlock = { mockBody(this@buildMockClass, mokkeryKind, it) }
    )
    return mockedClass
}

fun TransformerScope.buildManyMockClass(classesToMock: List<IrClass>): IrClass {
    val manyMocksMarkerClass = getClass(Mokkery.Class.mockMany(classesToMock.size))
    val mokkeryInstanceClass = getClass(Mokkery.Class.MokkeryInstanceScope)
    val mockedClass = pluginContext.irFactory
        .buildClass { name = manyMocksMarkerClass.kotlinFqName.createUniqueManyMockName() }
    classesToMock.forEach(mockedClass::copyTypeParametersFrom)
    mockedClass.createParameterDeclarations()
    mockedClass.origin = Mokkery.Origin
    val parameterMap = classesToMock.createParametersMapTo(mockedClass)
    val mockedTypes = classesToMock.typeWith(parameterMap)
    val manyMocksMarkerType = manyMocksMarkerClass.symbol.typeWith(mockedTypes)
    mockedClass.superTypes = mockedTypes + listOfNotNull(
        mokkeryInstanceClass.defaultType,
        if (classesToMock.all(IrClass::isInterface)) pluginContext.irBuiltIns.anyType else null,
        manyMocksMarkerType
    )
    mockedClass.addMockClassConstructor(
        transformer = this,
        scopeInstanceClass = mokkeryInstanceClass,
        mokkeryKind = IrMokkeryKind.Mock,
        typeName = mockManyTypeName(manyMocksMarkerClass, classesToMock),
        classesToIntercept = classesToMock,
    )
    classesToMock.flatMap { it.overridableFunctions }
        .groupBy { it.signatureString(true) }
        .map { (_, functions) ->
            mockedClass.addOverridingMethod(pluginContext, functions, parameterMap) {
                mockBody(this@buildManyMockClass, IrMokkeryKind.Mock, it)
            }
        }
    classesToMock.flatMap { it.overridableProperties }
        .groupBy { it.signatureString(true) }
        .map { (_, properties) ->
            mockedClass.addOverridingProperty(
                context = pluginContext,
                properties = properties,
                parameterMap = parameterMap,
                getterBlock = { mockBody(this@buildManyMockClass, IrMokkeryKind.Mock, it) },
                setterBlock = { mockBody(this@buildManyMockClass, IrMokkeryKind.Mock, it) }
            )
        }
    return mockedClass
}

private fun mockManyTypeName(klass: IrClass, types: List<IrClass>): String {
    return "${klass.kotlinFqName.asString()}<${types.joinToString { it.kotlinFqName.asString() }}>"
}

private fun List<IrClass>.createParametersMapTo(cls: IrClass): Map<IrTypeParameter, IrTypeParameter> {
    return memoryOptimizedFlatMap { it.typeParameters }
        .memoryOptimizedZip(cls.typeParameters)
        .toMap()
}

private fun List<IrClass>.typeWith(parameterMap: Map<IrTypeParameter, IrTypeParameter>): List<IrType> {
    return memoryOptimizedMap {
        it.symbol.typeWithParameters(it.typeParameters.memoryOptimizedMap(parameterMap::getValue))
    }
}

private fun IrBlockBodyBuilder.mockBody(
    transformer: TransformerScope,
    mokkeryKind: IrMokkeryKind,
    function: IrSimpleFunction,
) {
    +irReturn(irInterceptMethod(transformer, mokkeryKind, function))
}

private fun IrClass.addMockClassConstructor(
    transformer: TransformerScope,
    scopeInstanceClass: IrClass,
    mokkeryKind: IrMokkeryKind,
    typeName: String,
    classesToIntercept: List<IrClass>,
) {
    val context = transformer.pluginContext
    val mokkeryScopeClass = transformer.getClass(Mokkery.Class.MokkeryScope)
    val mockModeClass = transformer.getClass(Mokkery.Class.MockMode)
    val mokkeryKindClass = transformer.getClass(Mokkery.Class.MokkeryKind)
    val invokeInstantiationCallbacksFun = transformer.getFunction(Mokkery.Function.invokeInstantiationListener)
    val contextProperty = overridePropertyBackingField(context, scopeInstanceClass.getProperty("mokkeryContext"))
    addConstructor {
        isPrimary = true
    }.apply {
        addValueParameter("parent", mokkeryScopeClass.defaultType)
        addValueParameter("mode", mockModeClass.defaultType)
        addValueParameter("block", context.irBuiltIns.functionN(1).defaultTypeErased.makeNullable())
        val spyParam = when (mokkeryKind) {
            IrMokkeryKind.Spy -> addSpyParameter(classesToIntercept)
            IrMokkeryKind.Mock -> null
        }
        val kClassType = context.irBuiltIns.kClassClass.starProjectedType
        val typeKClassParameters = classesToIntercept
            .memoryOptimizedFlatMap(IrClass::typeParameters)
            .memoryOptimizedMapIndexed { index, it -> addValueParameter("type$index", kClassType) }
        body = DeclarationIrBuilder(context, symbol).irBlockBody {
            +irDelegatingDefaultConstructorOrAny(transformer, classesToIntercept.firstOrNull { it.isClass })
            +irSetPropertyField(
                thisParam = thisReceiver!!,
                property = contextProperty,
                value = irCall(transformer.getFunction(Mokkery.Function.createMokkeryInstanceContext)) {
                    extensionReceiver = irGet(valueParameters[0])
                    putValueArgument(0, irString(typeName))
                    putValueArgument(1, irGet(valueParameters[1]))
                    putValueArgument(2, irMokkeryKindValue(mokkeryKindClass, mokkeryKind))
                    putValueArgument(
                        index = 3,
                        valueArgument = irCallListOf(
                            transformerScope = transformer,
                            type = context.irBuiltIns.kClassClass.starProjectedType,
                            expressions = classesToIntercept.map { kClassReference(it.defaultTypeErased) }
                        )
                    )
                    putValueArgument(
                        index = 4,
                        valueArgument = irCallListOf(
                            transformerScope = transformer,
                            type = context.irBuiltIns.kClassClass.starProjectedType,
                            expressions = typeKClassParameters.memoryOptimizedMap { irGet(it) }
                        )
                    )
                    putValueArgument(5, irGet(thisReceiver!!))
                    putValueArgument(6, spyParam?.let(::irGet) ?: irNull())
                }
            )
            +irCall(invokeInstantiationCallbacksFun) {
                extensionReceiver = irGet(thisReceiver!!)
                putValueArgument(0, irGet(thisReceiver!!))
            }
            +irInvokeIfNotNull(irGet(valueParameters[2]), false, irGet(thisReceiver!!))
        }
    }
    addOverridingMethod(context, context.irBuiltIns.memberToString.owner) {
        +irReturn(irCall(transformer.getProperty(Mokkery.Property.mockIdString).getter!!.symbol) {
            extensionReceiver = irGet(it.dispatchReceiverParameter!!)
        })
    }
}

private fun IrConstructor.addSpyParameter(classesToIntercept: List<IrClass>): IrValueParameter {
    val classToSpy = classesToIntercept.singleOrNull() ?: error("Spy is not supported for intercepting multiple types!")
    return addValueParameter("obj", classToSpy.symbol.typeWithParameters(parentAsClass.typeParameters))
}

private fun Name.createUniqueMockName(type: String) = asString()
    .plus(type)
    .plus(UUID.randomUUID().toString().replace("-", ""))
    .let(Name::identifier)

private fun FqName.createUniqueManyMockName() = shortName()
    .asString()
    .plus(UUID.randomUUID().toString().replace("-", ""))
    .let(Name::identifier)
