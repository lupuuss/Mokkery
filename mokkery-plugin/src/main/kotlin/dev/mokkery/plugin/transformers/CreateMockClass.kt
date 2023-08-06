package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.buildClass
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.eraseFullValueParametersList
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irInvokeIfNotNull
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isInterface

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

private fun IrBlockBodyBuilder.mockBody(transformer: TransformerScope, function: IrSimpleFunction) {
    function.eraseFullValueParametersList()
    +irReturn(irInterceptMethod(transformer, function))
}

