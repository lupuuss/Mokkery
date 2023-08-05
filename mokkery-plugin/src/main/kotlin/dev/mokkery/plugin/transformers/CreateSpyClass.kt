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
import dev.mokkery.plugin.ir.irTryCatchAny
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import dev.mokkery.plugin.ir.overridePropertyBackingField
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
    val typeToMockErased = classToSpy.defaultTypeErased
    val mokkerySpyScopeClass = getClass(Mokkery.Class.MokkerySpyScope)
    val spiedClass = pluginContext.irFactory.buildClass(
        classToSpy.createUniqueMockName("Spy"),
        typeToMockErased,
        mokkerySpyScopeClass.defaultType,
        if (classToSpy.isInterface) pluginContext.irBuiltIns.anyType else null
    )
    currentFile.addChild(spiedClass)
    val delegateField = spiedClass.addField(fieldName = "delegate", typeToMockErased)
    val interceptor = spiedClass.overridePropertyBackingField(
        context = pluginContext,
        property = mokkerySpyScopeClass.getProperty("interceptor")
    )
    val idProperty = spiedClass.overridePropertyBackingField(
        context = pluginContext,
        property = mokkerySpyScopeClass.getProperty("id")
    )
    spiedClass.addConstructor { isPrimary = true }.apply {
        addValueParameter("obj", typeToMockErased)
        body = declarationIrBuilder(symbol) {
            irBlockBody {
                +irDelegatingDefaultConstructorOrAny(classToSpy)
                val identifierCall = irCall(getFunction(Mokkery.Function.generateMockId)) {
                    putValueArgument(0, irString(classToSpy.kotlinFqName.asString()))
                }
                val initializerCall = irCall(getFunction(Mokkery.Function.MokkerySpy))
                +irSetField(irGet(spiedClass.thisReceiver!!), delegateField, irGet(valueParameters[0]))
                +irSetField(irGet(spiedClass.thisReceiver!!), interceptor.backingField!!, initializerCall)
                +irSetField(irGet(spiedClass.thisReceiver!!), idProperty.backingField!!, identifierCall)
            }
        }
    }
    spiedClass.addOverridingMethod(pluginContext, pluginContext.irBuiltIns.memberToString.owner) {
        val getIdCall = irCall(idProperty.getter!!.symbol) {
            dispatchReceiver = irGet(it.dispatchReceiverParameter!!)
        }
        +irReturn(getIdCall)
    }
    spiedClass.overrideAllOverridableFunctions(pluginContext, classToSpy) {
        spyingBody(this@createSpyClass, delegateField, it)
    }
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
            val callOriginalMethod = irCall(function.overriddenSymbols.first()) {
                dispatchReceiver = irGetField(irGet(function.dispatchReceiverParameter!!), delegateField)
                function.valueParameters.forEachIndexed { index, irValueParameter ->
                    putValueArgument(index, irGet(irValueParameter))
                }
                function.extensionReceiverParameter?.let {
                    extensionReceiver = irGet(it)
                }
            }
            +irReturn(callOriginalMethod)
        }
    )
}
