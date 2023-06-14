package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.buildClass
import dev.mokkery.plugin.ext.createUniqueMockName
import dev.mokkery.plugin.ext.irCallConstructor
import dev.mokkery.plugin.ext.locationInFile
import dev.mokkery.plugin.ext.overrideAllOverridableFunctions
import dev.mokkery.plugin.ext.overrideAllOverridableProperties
import dev.mokkery.plugin.info
import dev.mokkery.plugin.mokkeryError
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.backend.js.utils.asString
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
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor

class SpyCallsTransformer(
    pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val irFile: IrFile,
    private val spyTable: MutableMap<IrClass, IrClass>,
) : MokkeryBaseTransformer(pluginContext) {

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        if (function.kotlinFqName != Mokkery.Function.spy) return super.visitCall(expression)
        val typeToSpy = expression.typeArguments.firstOrNull()?.takeIf { !it.isTypeParameter() } ?: mokkeryError {
            "Spy call must be direct! It can't be a type parameter! Failed at: ${expression.locationInFile(irFile)}"
        }
        if (!typeToSpy.isInterface()) {
            mokkeryError {
                "Only interfaces are currently supported! Failed at: ${expression.locationInFile(irFile)}"
            }
        }
        val classToSpy = typeToSpy.getClass()!!
        messageCollector.info {
            "Recognized spy call with type ${typeToSpy.asString()} at: ${expression.locationInFile(irFile)}"
        }
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
        val newClass = pluginContext.irFactory.buildClass(
            classToMock.createUniqueMockName("Spy"),
            classToMock.defaultType,
            irClasses.MokkerySpyScope.defaultType,
            pluginContext.irBuiltIns.anyType
        )
        val delegateField = newClass.addField(fieldName = "delegate", classToMock.defaultType)
        newClass.inheritMokkeryInterceptor(irClasses.MokkerySpyScope, classToMock) { constructor ->
            constructor.addValueParameter("obj", classToMock.defaultType)
            +irSetField(irGet(newClass.thisReceiver!!), delegateField, irGet(constructor.valueParameters[0]))
            irCall(irFunctions.MokkerySpy)
        }
        newClass.overrideAllOverridableFunctions(pluginContext, classToMock) {
            spyingBody(delegateField, it)
        }
        newClass.overrideAllOverridableProperties(
            context = pluginContext,
            superClass = classToMock,
            getterBlock = { spyingBody(delegateField, it) },
            setterBlock = { spyingBody(delegateField, it) }
        )
        return newClass
    }

    private fun IrBlockBodyBuilder.spyingBody(delegateField: IrField, function: IrSimpleFunction) {
        val interceptionResult = createTmpVariable(irCallInterceptingMethod(function))
        +irIfThenElse(
            type = function.returnType,
            condition = irCallIsTemplatingEnabled(function),
            thenPart = irReturn(irGet(interceptionResult)),
            elsePart = irReturn(irCall(function).apply {
                dispatchReceiver = irGetField(irGet(function.dispatchReceiverParameter!!), delegateField)
                function.valueParameters.forEachIndexed { index, irValueParameter ->
                    putValueArgument(index, irGet(irValueParameter))
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
