package dev.mokkery.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.overrides.isOverridableProperty
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isPrimitiveType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.irConstructorCall
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isMethodOfAny
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name

class MockCallsTransformer(
    private val pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val irFile: IrFile,
    private val mockTable: MutableMap<IrClass, IrClass>,
) : IrElementTransformerVoid() {

    private val mokkeryClass = MokkeryDeclarations.irClass(pluginContext)
    private val mokkeryScopeClass = MokkeryDeclarations.baseMokkeryScopeClass(pluginContext)

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        if (function.kotlinFqName != MokkeryDeclarations.mockFunctionName) return super.visitCall(expression)
        val typeToMock = expression.typeArguments.firstOrNull()
            ?: mokkeryError("Missing type argument for mock call at: ${expression.locationInFile(irFile)}")
        if (!typeToMock.isInterface()) {
            mokkeryError("Only interfaces are currently supported! Failed at: ${expression.locationInFile(irFile)}")
        }
        val classToMock = typeToMock.getClass()!!
        messageCollector.info("Mock call with type ${typeToMock.asString()} at ${expression.locationInFile(irFile)}")
        val mockedClass = mockTable.getOrPut(classToMock) {
            declareMock(classToMock).also {
                irFile.addChild(it)
            }
        }
        return irConstructorCall(expression, mockedClass.defaultConstructor!!.symbol)
    }

    private fun declareMock(classToMock: IrClass): IrClass {
        val newClass = pluginContext.irFactory.buildClass { name = classToMock.createMockName() }
        newClass.superTypes = listOf(classToMock.defaultType, mokkeryScopeClass.defaultType)
        newClass.thisReceiver = newClass.buildThisValueParam()
        newClass.addMokkeryScopeConstructor(classToMock.defaultType)
        classToMock
            .overridableFunctions
            .forEach { overridableFun ->
                if (!overridableFun.returnType.isPrimitiveType()) {
                    overridableFun.returnType = overridableFun.returnType.makeNullable()
                }
                newClass.addFunOverrideWithMockInterception(overridableFun)
            }
        classToMock
            .overridableProperties
            .forEach { overridableProperty ->
                newClass.addPropertyOverrideWithMockInterception(overridableProperty)
            }
        return newClass
    }

    private fun IrClass.addMokkeryScopeConstructor(typeToMock: IrType) {
        addConstructor {
            isPrimary = true
        }.apply {
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                +irDelegatingConstructorCall(mokkeryScopeClass.constructors.first().owner).apply {
                    putValueArgument(0, kClassReferenceUnified(pluginContext, typeToMock))
                }
            }
        }
    }

    private fun IrClass.addFunOverrideWithMockInterception(superFunction: IrSimpleFunction) {
        addOverridingMethod(superFunction) { generateMockInterception(it) }
    }

    private fun IrClass.addPropertyOverrideWithMockInterception(overridableProperty: IrProperty) {
        addOverridingProperty(
            property = overridableProperty,
            getterBlock = { generateMockInterception(it) },
            setterBlock = { generateMockInterception(it) }
        )
    }

    private fun IrClass.addOverridingMethod(
        function: IrSimpleFunction,
        block: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit
    ) {
        addFunction {
            name = function.name
            returnType = function.returnType
            isSuspend = function.isSuspend
            isInfix = function.isInfix
            isOperator = function.isOperator
            modality = Modality.FINAL
            origin = IrDeclarationOrigin.DEFINED
        }.apply {
            overriddenSymbols = listOf(function.symbol)
            typeParameters = function.typeParameters
            valueParameters = function.valueParameters
            dispatchReceiverParameter = buildThisValueParam()
            extensionReceiverParameter = function.extensionReceiverParameter
            contextReceiverParametersCount = function.contextReceiverParametersCount
            setDeclarationsParent(this@addOverridingMethod)
            body = DeclarationIrBuilder(pluginContext, symbol)
                .irBlockBody { block(this@apply) }
        }
    }

    private fun IrClass.addOverridingProperty(
        property: IrProperty,
        getterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
        setterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
    ) {
        addProperty {
            name = property.name
            isVar = property.isVar
            modality = Modality.FINAL
            origin = IrDeclarationOrigin.DEFINED
        }.apply {
            overriddenSymbols = listOf(property.symbol)
            addGetter().also {
                it.returnType = property.getter!!.returnType
                it.dispatchReceiverParameter = buildThisValueParam()
                it.body = DeclarationIrBuilder(pluginContext, it.symbol).irBlockBody { getterBlock(it) }
            }
            if (property.isVar) {
                addSetter().also {
                    it.returnType = property.setter!!.returnType
                    it.dispatchReceiverParameter = buildThisValueParam()
                    it.body = DeclarationIrBuilder(pluginContext, it.symbol).irBlockBody { setterBlock(it) }
                }
            }
            setDeclarationsParent(this@addOverridingProperty)
        }
    }

    private val IrClass.overridableFunctions
        get() = functions.filter { it.isOverridable && !it.isMethodOfAny() }

    private val IrClass.overridableProperties
        get() = properties.filter { it.isOverridableProperty() }

    private fun IrBlockBodyBuilder.generateMockInterception(function: IrSimpleFunction) {
        val thisParam = irGet(function.dispatchReceiverParameter!!)
        val mokkeryCall = if (function.isSuspend) {
            irCall(mokkeryClass.functionByName("interceptSuspendCall"))
        } else {
            irCall(mokkeryClass.functionByName("interceptCall"))
        }
        mokkeryCall.dispatchReceiver = irCall(mokkeryScopeClass.getPropertyGetter("mokkery")!!).apply {
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
}
