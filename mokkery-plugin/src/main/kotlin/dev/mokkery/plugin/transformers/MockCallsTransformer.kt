package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.addOverridingMethod
import dev.mokkery.plugin.ext.addOverridingProperty
import dev.mokkery.plugin.ext.buildThisValueParam
import dev.mokkery.plugin.ext.getProperty
import dev.mokkery.plugin.ext.irAnyVarargParams
import dev.mokkery.plugin.ext.irCallConstructor
import dev.mokkery.plugin.ext.irCallHashCodeIf
import dev.mokkery.plugin.ext.kClassReferenceUnified
import dev.mokkery.plugin.ext.locationInFile
import dev.mokkery.plugin.ext.nonGenericReturnTypeOrAny
import dev.mokkery.plugin.info
import dev.mokkery.plugin.mokkeryError
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addDefaultGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.overrides.isOverridableProperty
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isMethodOfAny
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name

class MockCallsTransformer(
    private val pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val irFile: IrFile,
    private val mockTable: MutableMap<IrClass, IrClass>,
) : IrElementTransformerVoid() {

    private val mokkeryMockClass = Mokkery.mokkeryMockClass(pluginContext)
    private val mokkeryMockScopeClass = Mokkery.mokkeryMockScopeClass(pluginContext)

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        if (function.kotlinFqName != Mokkery.mockFunctionName) return super.visitCall(expression)
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
                val modeArg = expression.valueArguments.getOrNull(0) ?: Mokkery.mockModeDefault(pluginContext, this)
                it.putValueArgument(0, modeArg)
            }
        }
    }

    private fun declareMock(classToMock: IrClass): IrClass {
        val newClass = pluginContext.irFactory.buildClass { name = classToMock.createMockName() }
        newClass.superTypes = listOf(
            classToMock.defaultType,
            mokkeryMockScopeClass.defaultType,
            pluginContext.irBuiltIns.anyType
        )
        newClass.thisReceiver = newClass.buildThisValueParam()
        val interceptorProperty = newClass.overridePropertyWithBackingField(mokkeryMockScopeClass.getProperty("interceptor"))
        val idProperty = newClass.overridePropertyWithBackingField(mokkeryMockScopeClass.getProperty("id"))
        newClass.addDefaultConstructor(interceptorProperty, idProperty, classToMock)
        newClass.addOverridingMethod(pluginContext, pluginContext.irBuiltIns.memberToString.owner) {
            +irReturn(irCall(idProperty.getter!!.symbol).apply {
                dispatchReceiver = irGet(it.dispatchReceiverParameter!!)
            })
        }
        classToMock
            .overridableFunctions
            .forEach { overridableFun ->
                newClass.addFunOverrideWithMockInterception(overridableFun)
            }
        classToMock
            .overridableProperties
            .forEach { overridableProperty ->
                newClass.addPropertyOverrideWithMockInterception(overridableProperty)
            }
        return newClass
    }

    private fun IrClass.addDefaultConstructor(
        interceptorProperty: IrProperty,
        idProperty: IrProperty,
        classToMock: IrClass
    ) {
        addConstructor {
            isPrimary = true
        }.apply {
            addValueParameter("mode", Mokkery.mockModeClass(pluginContext).defaultType)
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                +irDelegatingConstructorCall(pluginContext.irBuiltIns.anyClass.owner.primaryConstructor!!)
                val id = createTmpVariable(createIdCall(this, classToMock))
                val mokkeryMockCall = irCall(Mokkery.mokkeryMockFunction(pluginContext))
                mokkeryMockCall.putValueArgument(0, irGet(id))
                mokkeryMockCall.putValueArgument(1, irGet(valueParameters[0]))
                +irSetField(irGet(thisReceiver!!), interceptorProperty.backingField!!, mokkeryMockCall)
                +irSetField(irGet(thisReceiver!!), idProperty.backingField!!, irGet(id))
            }
        }
    }

    private fun IrClass.addFunOverrideWithMockInterception(superFunction: IrSimpleFunction) {
        addOverridingMethod(pluginContext, superFunction) { generateMockInterception(it) }
    }

    private fun IrClass.addPropertyOverrideWithMockInterception(overridableProperty: IrProperty) {
        addOverridingProperty(
            context = pluginContext,
            property = overridableProperty,
            getterBlock = { generateMockInterception(it) },
            setterBlock = { generateMockInterception(it) }
        )
    }

    private fun IrClass.overridePropertyWithBackingField(property: IrProperty): IrProperty {
        return addProperty {
            name = property.name
            isVar = property.isVar
            modality = Modality.FINAL
            origin = IrDeclarationOrigin.DEFINED
        }.apply {
            addBackingField {
                type = property.getter!!.returnType
            }
            overriddenSymbols = listOf(property.symbol)
            addDefaultGetter(this@overridePropertyWithBackingField, pluginContext.irBuiltIns)
            getter?.overriddenSymbols = listOf(property.getter!!.symbol)
        }
    }

    private val IrClass.overridableFunctions
        get() = functions.filter { it.isOverridable && !it.isMethodOfAny() }

    private val IrClass.overridableProperties
        get() = properties.filter { it.isOverridableProperty() }

    private fun IrBlockBodyBuilder.generateMockInterception(function: IrSimpleFunction) {
        val thisParam = irGet(function.dispatchReceiverParameter!!)
        val mokkeryCall = if (function.isSuspend) {
            irCall(mokkeryMockClass.functionByName("interceptSuspendCall"))
        } else {
            irCall(mokkeryMockClass.functionByName("interceptCall"))
        }
        mokkeryCall.dispatchReceiver = irCall(mokkeryMockScopeClass.getPropertyGetter("interceptor")!!).apply {
            dispatchReceiver = thisParam
        }
        mokkeryCall.putValueArgument(0, irString(function.stringSignature()))
        mokkeryCall.putValueArgument(
            index = 1,
            valueArgument = kClassReferenceUnified(pluginContext, function.nonGenericReturnTypeOrAny(pluginContext))
        )
        mokkeryCall.putValueArgument(2, irAnyVarargParams(function.valueParameters))
        +irReturn(mokkeryCall)
    }

    private fun IrClass.createMockName() = kotlinFqName
        .asString()
        .replace(".", "_")
        .plus("MokkeryMocked")
        .let(Name::identifier)

    private fun IrSimpleFunction.stringSignature(): String {
        return "${name.asString()}/${valueParameters.joinToString { it.type.asString() }}/${returnType.asString()}"
    }

    private fun IrClass.createIdCall(builder: IrBuilderWithScope, classToMock: IrClass): IrExpression {
        return builder.irConcat().apply {
            addArgument(builder.irString(classToMock.kotlinFqName.asString() + "@"))
            addArgument(builder.irCallHashCodeIf(this@createIdCall))
        }
    }
}
