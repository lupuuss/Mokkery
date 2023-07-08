package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.buildClass
import dev.mokkery.plugin.ext.createUniqueMockName
import dev.mokkery.plugin.ext.defaultTypeErased
import dev.mokkery.plugin.ext.eraseFullValueParametersList
import dev.mokkery.plugin.ext.irCallConstructor
import dev.mokkery.plugin.ext.irTryCatchAny
import dev.mokkery.plugin.ext.overrideAllOverridableFunctions
import dev.mokkery.plugin.ext.overrideAllOverridableProperties
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.declarations.addField
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor

class SpyCallsTransformer(
    pluginContext: IrPluginContext,
    messageCollector: MessageCollector,
    irFile: IrFile,
    private val spyTable: MutableMap<IrClass, IrClass>,
) : MokkeryBaseTransformer(pluginContext, messageCollector, irFile) {

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        if (function.kotlinFqName != Mokkery.Function.spy) return super.visitCall(expression)
        expression.checkInterceptionPossibilities(Mokkery.Function.spy)
        val typeToSpy = expression.typeArguments.first()!!
        val classToSpy = typeToSpy.getClass()!!
        val spiedClass = spyTable.getOrPut(classToSpy) {
            declareSpy(classToSpy).also {
                irFile.addChild(it)
            }
        }
        return DeclarationIrBuilder(pluginContext, expression.symbol).run {
            irCallConstructor(spiedClass.primaryConstructor!!).also {
                it.putValueArgument(0, expression.valueArguments[0])
            }
        }
    }

    private fun declareSpy(classToMock: IrClass): IrClass {
        val typeToMockProjected = classToMock.defaultTypeErased
        val newClass = pluginContext.irFactory.buildClass(
            classToMock.createUniqueMockName("Spy"),
            typeToMockProjected,
            irClasses.MokkeryMockScope.defaultType,
            if (classToMock.isInterface) pluginContext.irBuiltIns.anyType else null
        )
        val delegateField = newClass.addField(fieldName = "delegate", typeToMockProjected)
        newClass.inheritMokkeryInterceptor(
            interceptorScopeClass = irClasses.MokkerySpyScope,
            classToMock = classToMock,
            interceptorInit = { constructor ->
                constructor.addValueParameter("obj", typeToMockProjected)
                +irSetField(irGet(newClass.thisReceiver!!), delegateField, irGet(constructor.valueParameters[0]))
                irCall(irFunctions.MokkerySpy)
            }
        )
        newClass.overrideAllOverridableFunctions(pluginContext, classToMock) { spyingBody(delegateField, it) }
        newClass.overrideAllOverridableProperties(
            context = pluginContext,
            superClass = classToMock,
            getterBlock = { spyingBody(delegateField, it) },
            setterBlock = { spyingBody(delegateField, it) }
        )
        return newClass
    }

    private fun IrBlockBodyBuilder.spyingBody(delegateField: IrField, function: IrSimpleFunction) {
        function.eraseFullValueParametersList()
        val interceptingCall = if (function.returnType != pluginContext.irBuiltIns.nothingType) {
            irCallInterceptingMethod(function)
        } else {
            irTryCatchAny(irCallInterceptingMethod(function))
        }
        val interceptionResult = createTmpVariable(interceptingCall)
        +irIfThenElse(
            type = function.returnType,
            condition = irCallIsTemplatingEnabled(function),
            thenPart = irReturn(irGet(interceptionResult)),
            elsePart = irReturn(irCall(function.overriddenSymbols.first()).apply {
                dispatchReceiver = irGetField(irGet(function.dispatchReceiverParameter!!), delegateField)
                function.valueParameters.forEachIndexed { index, irValueParameter ->
                    putValueArgument(index, irGet(irValueParameter))
                }
                function.extensionReceiverParameter?.let {
                    extensionReceiver = irGet(it)
                }
            })
        )
    }

    private fun IrBlockBodyBuilder.irCallIsTemplatingEnabled(function: IrSimpleFunction): IrCall {
        val thisParam = irGet(function.dispatchReceiverParameter!!)
        val templatingAccess = irCall(irClasses.MokkerySpy.symbol.getPropertyGetter("templating")!!)
        templatingAccess.dispatchReceiver = irCall(irClasses.MokkerySpyScope.getPropertyGetter("interceptor")!!).apply {
            dispatchReceiver = thisParam
        }
        val isEnabled = irCall(irClasses.TemplatingInterceptor.symbol.getPropertyGetter("isEnabled")!!).apply {
            dispatchReceiver = templatingAccess
        }
        return isEnabled
    }
}
