package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.declarationIrBuilder
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.buildClass
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.eraseFullValueParametersList
import dev.mokkery.plugin.ir.getProperty
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irDelegatingDefaultConstructorOrAny
import dev.mokkery.plugin.ir.irInvokeIfNotNull
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import dev.mokkery.plugin.ir.overridePropertyBackingField
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName

fun TransformerScope.createMockClass(classToMock: IrClass): IrClass {
    val mockedClass = pluginContext.irFactory.buildClass(
        classToMock.createUniqueMockName("Mock"),
        classToMock.defaultTypeErased,
        getClass(Mokkery.Class.MokkeryMockScope).defaultType,
        if (classToMock.isInterface) pluginContext.irBuiltIns.anyType else null
    )
    currentFile.addChild(mockedClass)
    val mockInterceptorScopeClass = getClass(Mokkery.Class.MokkeryMockScope)
    val mockModeClass = getClass(Mokkery.Class.MockMode)
    val interceptor = mockedClass.overridePropertyBackingField(
        context = pluginContext,
        property = mockInterceptorScopeClass.getProperty("interceptor")
    )
    val idProperty = mockedClass.overridePropertyBackingField(
        context = pluginContext,
        property = mockInterceptorScopeClass.getProperty("id")
    )
    mockedClass.addConstructor { isPrimary = true }.apply {
        addValueParameter("mode", mockModeClass.defaultType)
        addValueParameter("block", pluginContext.irBuiltIns.functionN(1).defaultTypeErased)
        body = declarationIrBuilder(symbol) {
            irBlockBody {
                +irDelegatingDefaultConstructorOrAny(classToMock)
                val identifierCall = irCall(getFunction(Mokkery.Function.generateMockId)) {
                    putValueArgument(0, irString(classToMock.kotlinFqName.asString()))
                }
                val initializerCall = irCall(getFunction(Mokkery.Function.MokkeryMock)) {
                    putValueArgument(0, irGet(valueParameters[0]))
                }
                +irSetField(irGet(mockedClass.thisReceiver!!), interceptor.backingField!!, initializerCall)
                +irSetField(irGet(mockedClass.thisReceiver!!), idProperty.backingField!!, identifierCall)
                +irInvokeIfNotNull(irGet(valueParameters[1]), false, irGet(mockedClass.thisReceiver!!))
            }
        }
    }
    mockedClass.addOverridingMethod(pluginContext, pluginContext.irBuiltIns.memberToString.owner) {
        val getIdCall = irCall(idProperty.getter!!.symbol){
            dispatchReceiver = irGet(it.dispatchReceiverParameter!!)
        }
        +irReturn(getIdCall)
    }
    mockedClass.overrideAllOverridableFunctions(pluginContext, classToMock) { mockBody(this@createMockClass, it) }
    mockedClass.overrideAllOverridableProperties(
        context = pluginContext,
        superClass = classToMock,
        getterBlock = { mockBody(this@createMockClass, it) },
        setterBlock = { mockBody(this@createMockClass, it) }
    )
    return mockedClass
}

private fun IrBlockBodyBuilder.mockBody(transformer: TransformerScope, function: IrSimpleFunction) {
    function.eraseFullValueParametersList()
    +irReturn(irInterceptMethod(transformer, function))
}

