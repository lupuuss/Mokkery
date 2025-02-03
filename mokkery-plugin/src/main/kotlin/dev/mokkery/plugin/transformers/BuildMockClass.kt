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
import dev.mokkery.plugin.ir.getField
import dev.mokkery.plugin.ir.getProperty
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irDelegatingDefaultConstructorOrAny
import dev.mokkery.plugin.ir.irInvokeIfNotNull
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.irMokkeryKindValue
import dev.mokkery.plugin.ir.irSetPropertyField
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.overridableProperties
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import dev.mokkery.plugin.ir.overridePropertyBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerIr.signatureString
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.putArgument
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.createParameterDeclarations
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.makeTypeParameterSubstitutionMap
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.substitute
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
    val mokkeryMockInstanceClass = getClass(Mokkery.Class.MokkeryMockInstance)
    val mockedClass = pluginContext
        .irFactory
        .buildClass { name = classToMock.name.createUniqueMockName(mokkeryKind.name) }
    mockedClass.copyTypeParametersFrom(classToMock)
    val typedClassToMock = classToMock.symbol.typeWithParameters(mockedClass.typeParameters)
    mockedClass.superTypes = listOfNotNull(
        typedClassToMock,
        mokkeryMockInstanceClass.defaultType,
        if (classToMock.isInterface) pluginContext.irBuiltIns.anyType else null
    )
    mockedClass.createParameterDeclarations()
    mockedClass.origin = Mokkery.Origin
    mockedClass.addMockClassConstructor(
        transformer = this,
        typeName = classToMock.kotlinFqName.asString(),
        mokkeryKind = mokkeryKind,
        mokkeryInstanceClass = mokkeryMockInstanceClass,
        classesToIntercept = listOf(classToMock),
    )
    val spyDelegateField = mockedClass.getField(Mokkery.Fields.SpyDelegate)
    mockedClass.overrideAllOverridableFunctions(pluginContext, classToMock) {
        mockBody(this@buildMockClass, it, spyDelegateField)
    }
    mockedClass.overrideAllOverridableProperties(
        context = pluginContext,
        superClass = classToMock,
        getterBlock = { mockBody(this@buildMockClass, it, spyDelegateField) },
        setterBlock = { mockBody(this@buildMockClass, it, spyDelegateField) }
    )
    return mockedClass
}

fun TransformerScope.buildManyMockClass(classesToMock: List<IrClass>): IrClass {
    val manyMocksMarkerClass = getClass(Mokkery.Class.mockMany(classesToMock.size))
    val mokkeryMockInstanceClass = getClass(Mokkery.Class.MokkeryMockInstance)
    val mockedClass = pluginContext.irFactory
        .buildClass { name = manyMocksMarkerClass.kotlinFqName.createUniqueManyMockName() }
    classesToMock.forEach(mockedClass::copyTypeParametersFrom)
    mockedClass.createParameterDeclarations()
    mockedClass.origin = Mokkery.Origin
    val parameterMap = classesToMock.createParametersMapTo(mockedClass)
    val mockedTypes = classesToMock.typeWith(parameterMap)
    val manyMocksMarkerType = manyMocksMarkerClass.symbol.typeWith(mockedTypes)
    mockedClass.superTypes = mockedTypes + listOfNotNull(
        mokkeryMockInstanceClass.defaultType,
        if (classesToMock.all(IrClass::isInterface)) pluginContext.irBuiltIns.anyType else null,
        manyMocksMarkerType
    )
    mockedClass.addMockClassConstructor(
        transformer = this,
        mokkeryInstanceClass = mokkeryMockInstanceClass,
        mokkeryKind = IrMokkeryKind.Mock,
        typeName = mockManyTypeName(manyMocksMarkerClass, classesToMock),
        classesToIntercept = classesToMock,
    )
    classesToMock.flatMap { it.overridableFunctions }
        .groupBy { it.signatureString(true) }
        .map { (_, functions) ->
            mockedClass.addOverridingMethod(pluginContext, functions, parameterMap) {
                mockBody(this@buildManyMockClass, it, null)
            }
        }
    classesToMock.flatMap { it.overridableProperties }
        .groupBy { it.signatureString(true) }
        .map { (_, properties) ->
            mockedClass.addOverridingProperty(
                context = pluginContext,
                properties = properties,
                parameterMap = parameterMap,
                getterBlock = { mockBody(this@buildManyMockClass, it, null) },
                setterBlock = { mockBody(this@buildManyMockClass, it, null) }
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
    function: IrSimpleFunction,
    spyDelegateField: IrField?,
) {
    val superCallLambda = spyDelegateField?.let { irLambdaSpyCall(transformer, it, function) }
    +irReturn(irInterceptMethod(transformer, function, superCallLambda))
}

private fun IrClass.addMockClassConstructor(
    transformer: TransformerScope,
    mokkeryInstanceClass: IrClass,
    mokkeryKind: IrMokkeryKind,
    typeName: String,
    classesToIntercept: List<IrClass>,
) {
    val context = transformer.pluginContext
    val mokkeryMockInterceptorFun = transformer.getFunction(Mokkery.Function.MokkeryMockInterceptor)
    val mokkeryScopeClass = transformer.getClass(Mokkery.Class.MokkeryScope)
    val mockModeClass = transformer.getClass(Mokkery.Class.MockMode)
    val mokkeryKindClass = transformer.getClass(Mokkery.Class.MokkeryKind)
    val registerMockFun = transformer.getFunction(Mokkery.Function.registerMock)
    val interceptor = overridePropertyBackingField(context, mokkeryInstanceClass.getProperty("mokkeryInterceptor"))
    val contextProperty = overridePropertyBackingField(context, mokkeryInstanceClass.getProperty("mokkeryContext"))
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
                property = interceptor,
                value = irCall(mokkeryMockInterceptorFun)
            )
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
                    putValueArgument(4, irGet(thisReceiver!!))
                }
            )
            if (spyParam != null) {
                +irSetField(
                    receiver = irGet(thisReceiver!!),
                    field = addField(fieldName = Mokkery.Fields.SpyDelegate, fieldType = spyParam.type),
                    value = irGet(spyParam)
                )
            }
            typeKClassParameters.forEachIndexed { index, it ->
                +irSetField(
                    receiver = irGet(thisReceiver!!),
                    field = addField(fieldName = Mokkery.Fields.typeArg(index), fieldType = it.type),
                    value = irGet(it)
                )
            }
            +irCall(registerMockFun) {
                extensionReceiver = irGet(valueParameters[0])
                putValueArgument(0, irGet(thisReceiver!!))
            }
            +irInvokeIfNotNull(irGet(valueParameters[2]), false, irGet(thisReceiver!!))
        }
    }
    addOverridingMethod(context, context.irBuiltIns.memberToString.owner) {
        +irReturn(irCall(transformer.getProperty(Mokkery.Property.mockId).getter!!.symbol) {
            extensionReceiver = irGet(it.dispatchReceiverParameter!!)
        })
    }
}

private fun IrConstructor.addSpyParameter(classesToIntercept: List<IrClass>): IrValueParameter {
    val classToSpy = classesToIntercept.singleOrNull() ?: error("Spy is not supported for intercepting multiple types!")
    return addValueParameter("obj", classToSpy.symbol.typeWithParameters(parentAsClass.typeParameters))
}

private fun IrBlockBodyBuilder.irLambdaSpyCall(
    transformer: TransformerScope,
    delegateField: IrField,
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
        val spyFun = function.overriddenSymbols.first().owner
        val typesMap = makeTypeParameterSubstitutionMap(spyFun, function)
        val spyCall = irCall(spyFun, spyFun.returnType.substitute(typesMap)) {
            dispatchReceiver = irGetField(irGet(function.dispatchReceiverParameter!!), delegateField)
            function.typeParameters.forEachIndexed { i, type -> putTypeArgument(i, type.defaultType) }
            spyFun.fullValueParameterList.forEachIndexed { index, irValueParameter ->
                putArgument(
                    parameter = irValueParameter,
                    argument = irAs(
                        argument = irCall(context.irBuiltIns.listClass.owner.getSimpleFunction("get")!!) {
                            dispatchReceiver = irGet(lambda.valueParameters[0])
                            putValueArgument(0, irInt(index))
                        },
                        type = irValueParameter.type.substitute(typesMap)
                    )
                )
            }
        }
        +irReturn(spyCall)
    }
}

private fun Name.createUniqueMockName(type: String) = asString()
    .plus(type)
    .plus(UUID.randomUUID().toString().replace("-", ""))
    .let(Name::identifier)

private fun FqName.createUniqueManyMockName() = shortName()
    .asString()
    .plus(UUID.randomUUID().toString().replace("-", ""))
    .let(Name::identifier)
