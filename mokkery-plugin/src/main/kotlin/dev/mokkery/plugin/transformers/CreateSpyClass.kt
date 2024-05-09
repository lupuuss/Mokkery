package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.buildClass
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.eraseFullValueParametersList
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.overrideAllOverridableFunctions
import dev.mokkery.plugin.ir.overrideAllOverridableProperties
import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.putArgument
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isInterface

fun TransformerScope.createSpyClass(classToSpy: IrClass): IrClass {
    val typeToMockErased = classToSpy.defaultTypeErased
    val mokkerySpyScopeClass = getClass(Mokkery.Class.MokkerySpyScope)
    val spiedClass = pluginContext.irFactory.buildClass(
        classToSpy.createUniqueMockName("Spy"),
        typeToMockErased,
        mokkerySpyScopeClass.defaultType,
        if (classToSpy.isInterface) pluginContext.irBuiltIns.anyType else null
    )
    spiedClass.origin = Mokkery.Origin
    val delegateField = spiedClass.addField(fieldName = "delegate", typeToMockErased)
    spiedClass.inheritMokkeryInterceptor(
        transformer = this,
        interceptorScopeClass = mokkerySpyScopeClass,
        classToIntercept = classToSpy,
        interceptorInit = { constructor ->
            constructor.addValueParameter("obj", typeToMockErased)
            +irSetField(irGet(spiedClass.thisReceiver!!), delegateField, irGet(constructor.valueParameters[0]))
            irCall(getFunction(Mokkery.Function.MokkerySpy))
        },
    )
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
    val irCallSpyLambda = irCallSpyLambda(transformer, delegateField, function)
    +irReturn(irInterceptMethod(transformer, function, irCallSpyLambda))
}

private fun IrBlockBodyBuilder.irCallSpyLambda(
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
        val spyCall = irCall(spyFun) {
            dispatchReceiver = irGetField(irGet(function.dispatchReceiverParameter!!), delegateField)
            contextReceiversCount = spyFun.contextReceiverParametersCount
            spyFun.fullValueParameterList.forEachIndexed { index, irValueParameter ->
                putArgument(
                    parameter = irValueParameter,
                    argument = irAs(
                        argument = irCall(context.irBuiltIns.listClass.owner.getSimpleFunction("get")!!) {
                            dispatchReceiver = irGet(lambda.valueParameters[0])
                            putValueArgument(0, irInt(index))
                        },
                        type = irValueParameter.type
                    )
                )
            }
        }
        +irReturn(spyCall)
    }
}