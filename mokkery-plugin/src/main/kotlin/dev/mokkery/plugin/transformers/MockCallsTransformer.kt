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
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor

class MockCallsTransformer(
    pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val irFile: IrFile,
    private val mockTable: MutableMap<IrClass, IrClass>,
) : MokkeryBaseTransformer(pluginContext) {

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        if (function.kotlinFqName != Mokkery.Function.mock) return super.visitCall(expression)
        val typeToMock = expression.typeArguments.firstOrNull()?.takeIf { !it.isTypeParameter() } ?: mokkeryError {
            "Mock call must be direct! It can't be a type parameter! Failed at: ${expression.locationInFile(irFile)}"
        }
        if (!typeToMock.isInterface()) {
            mokkeryError {
                "Only interfaces are currently supported! Failed at: ${expression.locationInFile(irFile)}"
            }
        }
        val classToMock = typeToMock.getClass()!!
        messageCollector.info {
            "Recognized mock call with type ${typeToMock.asString()} at: ${expression.locationInFile(irFile)}"
        }
        val mockedClass = mockTable.getOrPut(classToMock) {
            declareMock(classToMock).also {
                irFile.addChild(it)
            }
        }
        return DeclarationIrBuilder(pluginContext, expression.symbol).run {
            irCallConstructor(mockedClass.primaryConstructor!!).also {
                val modeArg = expression.valueArguments.getOrNull(0) ?: irCallMockModeDefault()
                it.putValueArgument(0, modeArg)
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
        newClass.inheritMokkeryInterceptor(irClasses.MokkeryMockScope, classToMock) { constructor ->
            constructor.addValueParameter("mode", irClasses.MockMode.defaultType)
            val mokkeryMockCall = irCall(irFunctions.MokkeryMock)
            mokkeryMockCall.putValueArgument(1, irGet(constructor.valueParameters[0]))
            mokkeryMockCall
        }
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
