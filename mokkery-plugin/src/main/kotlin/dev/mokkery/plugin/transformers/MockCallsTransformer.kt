package dev.mokkery.plugin.transformers

import dev.mokkery.MockMode
import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.buildClass
import dev.mokkery.plugin.ext.createUniqueMockName
import dev.mokkery.plugin.ext.irCallConstructor
import dev.mokkery.plugin.ext.irGetEnumEntry
import dev.mokkery.plugin.ext.overrideAllOverridableFunctions
import dev.mokkery.plugin.ext.overrideAllOverridableProperties
import dev.mokkery.plugin.infoAt
import dev.mokkery.plugin.mokkeryError
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.backend.common.lower.irNot
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irEqualsNull
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.invokeFun
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor

class MockCallsTransformer(
    pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val irFile: IrFile,
    private val mockTable: MutableMap<IrClass, IrClass>,
    private val mockMode: MockMode,
) : MokkeryBaseTransformer(pluginContext) {

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        if (function.kotlinFqName != Mokkery.Function.mock) return super.visitCall(expression)
        val typeToMock = expression.typeArguments.firstOrNull()?.takeIf { !it.isTypeParameter() }
            ?: expression.mokkeryError(irFile) { "Mock call must be direct! It can't be a type parameter!" }
        if (!typeToMock.isInterface()) {
            expression.mokkeryError(irFile) { "Only interfaces are currently supported!" }
        }
        val classToMock = typeToMock.getClass()!!
        messageCollector.infoAt(expression, irFile) {
            "Recognized mock call with type ${typeToMock.asString()}!"
        }
        val mockedClass = mockTable.getOrPut(classToMock) {
            declareMock(classToMock).also {
                irFile.addChild(it)
            }
        }
        return DeclarationIrBuilder(pluginContext, expression.symbol).run {
            irCallConstructor(mockedClass.primaryConstructor!!).also {
                val modeArg = expression.valueArguments
                    .getOrNull(0)
                    ?: irGetEnumEntry(irClasses.MockMode, mockMode.toString())
                it.putValueArgument(0, modeArg)
                it.putValueArgument(1, expression.valueArguments.getOrNull(1) ?: irNull())
            }
        }
    }

    private fun declareMock(classToMock: IrClass): IrClass {
        val newClass = pluginContext.irFactory.buildClass(
            classToMock.createUniqueMockName("Mock"),
            classToMock.defaultType,
            irClasses.MokkeryMockScope.defaultType,
            pluginContext.irBuiltIns.anyType
        )
        newClass.inheritMokkeryInterceptor(
            interceptorScopeClass = irClasses.MokkeryMockScope,
            classToMock = classToMock,
            interceptorInit = { constructor ->
                constructor.addValueParameter("mode", irClasses.MockMode.defaultType)
                constructor.addValueParameter("block", pluginContext.irBuiltIns.functionN(1).defaultType.makeNullable())
                irCall(irFunctions.MokkeryMock).apply {
                    putValueArgument(1, irGet(constructor.valueParameters[0]))
                }
            },
            block = {
                val param = it.valueParameters[1]
                +irIfThen(
                    condition = irNot(irEqualsNull(argument = irGet(param))),
                    thenPart = irCall(pluginContext.irBuiltIns.functionN(1).invokeFun!!).apply {
                        dispatchReceiver = irGet(param)
                        putValueArgument(0, irGet(newClass.thisReceiver!!))
                    }
                )
            }
        )
        newClass.overrideAllOverridableFunctions(pluginContext, classToMock) { +irReturn(irCallInterceptingMethod(it)) }
        newClass.overrideAllOverridableProperties(
            context = pluginContext,
            superClass = classToMock,
            getterBlock = { +irReturn(irCallInterceptingMethod(it)) },
            setterBlock = { +irReturn(irCallInterceptingMethod(it)) }
        )
        return newClass
    }
}
