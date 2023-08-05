package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ext.addOverridingMethod
import dev.mokkery.plugin.ext.buildClass
import dev.mokkery.plugin.ext.defaultTypeErased
import dev.mokkery.plugin.ext.eraseFullValueParametersList
import dev.mokkery.plugin.ext.getProperty
import dev.mokkery.plugin.ext.irDelegatingDefaultConstructorOrAny
import dev.mokkery.plugin.ext.irTryCatchAny
import dev.mokkery.plugin.ext.overrideAllOverridableFunctions
import dev.mokkery.plugin.ext.overrideAllOverridableProperties
import dev.mokkery.plugin.ext.overridePropertyBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName

fun TransformerScope.createSpyClass(classToSpy: IrClass): IrClass {
    val typeToMockProjected = classToSpy.defaultTypeErased
    val mokkerySpyScopeClass = getClass(Mokkery.Class.MokkerySpyScope)
    val spiedClass = pluginContext.irFactory.buildClass(
        classToSpy.createUniqueMockName("Spy"),
        typeToMockProjected,
        mokkerySpyScopeClass.defaultType,
        if (classToSpy.isInterface) pluginContext.irBuiltIns.anyType else null
    )
    currentFile.addChild(spiedClass)
    val delegateField = spiedClass.addField(fieldName = "delegate", typeToMockProjected)
    val interceptor = spiedClass
        .overridePropertyBackingField(pluginContext, mokkerySpyScopeClass.getProperty("interceptor"))
    val idProperty = spiedClass
        .overridePropertyBackingField(pluginContext, mokkerySpyScopeClass.getProperty("id"))
    spiedClass.addConstructor {
        isPrimary = true
    }.apply {
        addValueParameter("obj", typeToMockProjected)
        body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
            +irDelegatingDefaultConstructorOrAny(classToSpy)
            val identifierCall = irCall(getFunction(Mokkery.Function.generateMockId)).apply {
                putValueArgument(0, irString(classToSpy.kotlinFqName.asString()))
            }
            val initializerCall = irCall(getFunction(Mokkery.Function.MokkerySpy))
            +irSetField(irGet(spiedClass.thisReceiver!!), delegateField, irGet(valueParameters[0]))
            +irSetField(irGet(spiedClass.thisReceiver!!), interceptor.backingField!!, initializerCall)
            +irSetField(irGet(spiedClass.thisReceiver!!), idProperty.backingField!!, identifierCall)
        }
    }
    spiedClass.addOverridingMethod(pluginContext, pluginContext.irBuiltIns.memberToString.owner) {
        +irReturn(irCall(idProperty.getter!!.symbol).apply {
            dispatchReceiver = irGet(it.dispatchReceiverParameter!!)
        })
    }
    spiedClass.overrideAllOverridableFunctions(pluginContext, classToSpy) { spyingBody(this@createSpyClass, delegateField, it) }
    spiedClass.overrideAllOverridableProperties(
        context = pluginContext,
        superClass = classToSpy,
        getterBlock = { spyingBody(this@createSpyClass, delegateField, it) },
        setterBlock = { spyingBody(this@createSpyClass, delegateField, it) }
    )
    return spiedClass
}

private fun IrBlockBodyBuilder.spyingBody(
    transformer: TransformerScope,
    delegateField: IrField,
    function: IrSimpleFunction
) {
    function.eraseFullValueParametersList()
    +irIfThenElse(
        type = function.returnType,
        condition = irCallIsTemplatingEnabled(transformer, irGet(function.dispatchReceiverParameter!!)),
        thenPart = irReturn(irInterceptMethod(transformer, function)),
        elsePart = irBlock {
            +irTryCatchAny(irInterceptMethod(transformer, function))
            +irReturn(irCall(function.overriddenSymbols.first()).apply {
                dispatchReceiver = irGetField(irGet(function.dispatchReceiverParameter!!), delegateField)
                function.valueParameters.forEachIndexed { index, irValueParameter ->
                    putValueArgument(index, irGet(irValueParameter))
                }
                function.extensionReceiverParameter?.let {
                    extensionReceiver = irGet(it)
                }
            })
        }
    )
}
