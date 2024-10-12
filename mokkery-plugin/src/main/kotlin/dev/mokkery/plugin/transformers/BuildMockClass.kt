package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.IrMokkeryKind
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.addOverridingProperty
import dev.mokkery.plugin.ir.buildClass
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.eraseFullValueParametersList
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
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.putArgument
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.makeTypeParameterSubstitutionMap
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.util.*

fun TransformerScope.buildMockClass(
    mokkeryKind: IrMokkeryKind,
    classToMock: IrClass,
): IrClass {
    val mokkeryMockScopeClass = getClass(Mokkery.Class.MokkeryMockInstance)
    val typeToMockErased = classToMock.defaultTypeErased
    val mockedClass = pluginContext.irFactory.buildClass(
        classToMock.name.createUniqueMockName(mokkeryKind.name),
        typeToMockErased,
        mokkeryMockScopeClass.defaultType,
        if (classToMock.isInterface) pluginContext.irBuiltIns.anyType else null
    )
    var spyDelegateField: IrField? = null
    mockedClass.origin = Mokkery.Origin
    mockedClass.addMockClassConstructor(
        transformer = this,
        typeName = classToMock.name.asString(),
        mokkeryKind = mokkeryKind,
        scopeClass = mokkeryMockScopeClass,
        classesToIntercept = listOf(classToMock),
        block = { constructor ->
            if (mokkeryKind == IrMokkeryKind.Spy) {
                val field = mockedClass.addField(fieldName = "delegate", typeToMockErased)
                spyDelegateField = field
                +irSetField(
                    receiver = irGet(mockedClass.thisReceiver!!),
                    field = field,
                    value = irGet(constructor.addValueParameter("obj", typeToMockErased))
                )
            }
        }
    )
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
    val mockedTypes = classesToMock.map { it.defaultTypeErased }
    val manyMocksMarker = getClass(Mokkery.Class.mockMany(classesToMock.size)).typeWith(mockedTypes)
    val mokkeryMockScopeClass = getClass(Mokkery.Class.MokkeryMockInstance)
    val superTypes = mockedTypes + listOfNotNull(
        mokkeryMockScopeClass.defaultType,
        if (classesToMock.all(IrClass::isInterface)) pluginContext.irBuiltIns.anyType else null,
        manyMocksMarker
    )
    val mockedClass = pluginContext.irFactory.buildClass(
        name = manyMocksMarker.classFqName!!.createUniqueManyMockName(),
        superTypes = superTypes.toTypedArray()
    )
    mockedClass.origin = Mokkery.Origin
    mockedClass.addMockClassConstructor(
        transformer = this,
        scopeClass = mokkeryMockScopeClass,
        mokkeryKind = IrMokkeryKind.Mock,
        typeName = manyMocksMarker.render(),
        classesToIntercept = classesToMock,
    )
    classesToMock.flatMap { it.overridableFunctions }
        .groupBy { it.signatureString(true) }
        .map { (_, functions) ->
            mockedClass.addOverridingMethod(pluginContext, functions) {
                mockBody(this@buildManyMockClass, it, null)
            }
        }
    classesToMock.flatMap { it.overridableProperties }
        .groupBy { it.signatureString(true) }
        .map { (_, properties) ->
            mockedClass.addOverridingProperty(
                context = pluginContext,
                properties = properties,
                getterBlock = { mockBody(this@buildManyMockClass, it, null) },
                setterBlock = { mockBody(this@buildManyMockClass, it, null) }
            )
        }
    return mockedClass
}

private fun IrBlockBodyBuilder.mockBody(
    transformer: TransformerScope,
    function: IrSimpleFunction,
    spyDelegateField: IrField?
) {
    function.eraseFullValueParametersList()
    val superCallLambda = spyDelegateField?.let { irLambdaSpyCall(transformer, it, function) }
    +irReturn(irInterceptMethod(transformer, function, superCallLambda))
}

private fun IrClass.addMockClassConstructor(
    transformer: TransformerScope,
    scopeClass: IrClass,
    mokkeryKind: IrMokkeryKind,
    typeName: String,
    classesToIntercept: List<IrClass>,
    block: IrBlockBodyBuilder.(IrConstructor) -> Unit = { }
) {
    val context = transformer.pluginContext
    val mokkeryMock = transformer.getFunction(Mokkery.Function.MokkeryMockInterceptor)
    val mockModeClass = transformer.getClass(Mokkery.Class.MockMode)
    val mokkeryKindClass = transformer.getClass(Mokkery.Class.MokkeryKind)
    val interceptor = overridePropertyBackingField(context, scopeClass.getProperty("interceptor"))
    val idProperty = overridePropertyBackingField(context, scopeClass.getProperty("id"))
    val typesProperty = overridePropertyBackingField(context, scopeClass.getProperty("interceptedTypes"))
    addConstructor {
        isPrimary = true
    }.apply {
        addValueParameter("mode", mockModeClass.defaultType)
        addValueParameter("block", context.irBuiltIns.functionN(1).defaultTypeErased.makeNullable())
        body = DeclarationIrBuilder(context, symbol).irBlockBody {
            +irDelegatingDefaultConstructorOrAny(transformer, classesToIntercept.firstOrNull { it.isClass })
            +irSetPropertyField(
                thisParam = thisReceiver!!,
                property = typesProperty,
                value = irCallListOf(
                    transformerScope = transformer,
                    type = context.irBuiltIns.kClassClass.defaultType,
                    expressions = classesToIntercept.map { kClassReference(it.defaultTypeErased) }
                )
            )
            +irSetPropertyField(
                thisParam = thisReceiver!!,
                property = interceptor,
                value = irCall(mokkeryMock) {
                    putValueArgument(0, irGet(valueParameters[0]))
                    putValueArgument(1, irMokkeryKindValue(mokkeryKindClass, mokkeryKind))
                }
            )
            +irSetPropertyField(
                thisParam = thisReceiver!!,
                property = idProperty,
                value = irCall(transformer.getFunction(Mokkery.Function.generateMockId)) {
                    putValueArgument(0, irString(typeName))
                }
            )
            block(this@apply)
            +irInvokeIfNotNull(irGet(valueParameters[1]), false, irGet(thisReceiver!!))
        }
    }
    addOverridingMethod(context, context.irBuiltIns.memberToString.owner) {
        +irReturn(irCall(idProperty.getter!!.symbol) {
            dispatchReceiver = irGet(it.dispatchReceiverParameter!!)
        })
    }
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
            contextReceiversCount = spyFun.contextReceiverParametersCount
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
