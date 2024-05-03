package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.addOverridingProperty
import dev.mokkery.plugin.ir.buildClass
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.eraseFullValueParametersList
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irInvokeIfNotNull
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.overridableProperties
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerIr.signatureString
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.render

fun TransformerScope.createMockClass(classToMock: IrClass): IrClass {
    val mockedClass = pluginContext.irFactory.buildClass(
        classToMock.createUniqueMockName("Mock"),
        classToMock.defaultTypeErased,
        getClass(Mokkery.Class.MokkeryMockScope).defaultType,
        if (classToMock.isInterface) pluginContext.irBuiltIns.anyType else null
    )
    mockedClass.origin = Mokkery.Origin
    val mockInterceptorScopeClass = getClass(Mokkery.Class.MokkeryMockScope)
    val mockModeClass = getClass(Mokkery.Class.MockMode)
    mockedClass.inheritMokkeryInterceptor(
        transformer = this,
        classToIntercept = classToMock,
        interceptorScopeClass = mockInterceptorScopeClass,
        interceptorInit = { constructor ->
            constructor.addValueParameter("mode", mockModeClass.defaultType)
            constructor.addValueParameter("block", context.irBuiltIns.functionN(1).defaultTypeErased)
            irCall(getFunction(Mokkery.Function.MokkeryMock)) {
                putValueArgument(0, irGet(constructor.valueParameters[0]))
            }
        },
        block = { +irInvokeIfNotNull(irGet(it.valueParameters[1]), false, irGet(mockedClass.thisReceiver!!)) }
    )
    mockedClass.overrideAllOverridableFunctions(pluginContext, classToMock) { mockBody(this@createMockClass, it) }
    mockedClass.overrideAllOverridableProperties(
        context = pluginContext,
        superClass = classToMock,
        getterBlock = { mockBody(this@createMockClass, it) },
        setterBlock = { mockBody(this@createMockClass, it) }
    )
    return mockedClass
}

fun TransformerScope.createManyMockClass(classesToMock: List<IrClass>): IrClass {
    val mockedTypes = classesToMock.map { it.defaultTypeErased }
    val manyMocksMarker = getClass(Mokkery.Class.manyMocks(classesToMock.size)).typeWith(mockedTypes)
    val superTypes = mockedTypes + listOfNotNull(
        getClass(Mokkery.Class.MokkeryMockScope).defaultType,
        if (classesToMock.all { it.isInterface }) pluginContext.irBuiltIns.anyType else null,
        manyMocksMarker
    )
    val mockedClass = pluginContext.irFactory.buildClass(
        name = manyMocksMarker.createUniqueManyMockName(),
        superTypes = superTypes.toTypedArray()
    )
    mockedClass.origin = Mokkery.Origin
    val mockInterceptorScopeClass = getClass(Mokkery.Class.MokkeryMockScope)
    val mockModeClass = getClass(Mokkery.Class.MockMode)
    mockedClass.inheritMokkeryInterceptor(
        transformer = this,
        classesToIntercept = classesToMock,
        typeName = manyMocksMarker.render(),
        interceptorScopeClass = mockInterceptorScopeClass,
        interceptorInit = { constructor ->
            constructor.addValueParameter("mode", mockModeClass.defaultType)
            constructor.addValueParameter("block", context.irBuiltIns.functionN(1).defaultTypeErased)
            irCall(getFunction(Mokkery.Function.MokkeryMock)) {
                putValueArgument(0, irGet(constructor.valueParameters[0]))
            }
        },
        block = { +irInvokeIfNotNull(irGet(it.valueParameters[1]), false, irGet(mockedClass.thisReceiver!!)) }
    )
    classesToMock
        .flatMap { it.overridableFunctions }
        .groupBy { it.signatureString(true) }
        .map { (_, functions) ->
            mockedClass.addOverridingMethod(pluginContext, functions) { mockBody(this@createManyMockClass, it) }
        }
    classesToMock
        .flatMap { it.overridableProperties }
        .groupBy { it.signatureString(true) }
        .map { (_, properties) ->
            mockedClass.addOverridingProperty(
                context = pluginContext,
                properties = properties,
                getterBlock = { mockBody(this@createManyMockClass, it) },
                setterBlock = { mockBody(this@createManyMockClass, it) }
            )
        }
    return mockedClass
}

private fun IrBlockBodyBuilder.mockBody(transformer: TransformerScope, function: IrSimpleFunction) {
    function.eraseFullValueParametersList()
    +irReturn(irInterceptMethod(transformer, function))
}

