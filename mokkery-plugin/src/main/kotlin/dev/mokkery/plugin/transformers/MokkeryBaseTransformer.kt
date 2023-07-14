package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Kotlin
import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.addOverridingMethod
import dev.mokkery.plugin.ext.firstFunction
import dev.mokkery.plugin.ext.getClass
import dev.mokkery.plugin.ext.getProperty
import dev.mokkery.plugin.ext.irCallHashCode
import dev.mokkery.plugin.ext.irDelegatingDefaultConstructorOrAny
import dev.mokkery.plugin.ext.irToString
import dev.mokkery.plugin.ext.kClassReferenceUnified
import dev.mokkery.plugin.ext.nonGenericReturnTypeOrAny
import dev.mokkery.plugin.ext.overridePropertyBackingField
import dev.mokkery.plugin.logAt
import dev.mokkery.plugin.mokkeryError
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.overrides.isOverridableProperty
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly

abstract class MokkeryBaseTransformer(
    protected val pluginContext: IrPluginContext,
    protected val messageCollector: MessageCollector,
    protected val irFile: IrFile,
) : IrElementTransformerVoid() {

    protected val irClasses = IrClasses()
    protected val irFunctions = IrFunctions()

    protected fun IrBlockBodyBuilder.irCallInterceptingMethod(function: IrSimpleFunction): IrCall {
        val getThisParam = irGet(function.dispatchReceiverParameter!!)
        val mokkeryCall = if (function.isSuspend) {
            irCall(irClasses.MokkeryInterceptor.symbol.functionByName("interceptSuspendCall"))
        } else {
            irCall(irClasses.MokkeryInterceptor.symbol.functionByName("interceptCall"))
        }

        mokkeryCall.dispatchReceiver = irClasses
            .MokkeryInterceptorScope
            .getPropertyGetter("interceptor")!!
            .let(::irCall)
            .apply {
                dispatchReceiver = getThisParam
            }
        val contextCreationCall = irCall(irClasses.CallContext.primaryConstructor!!).apply {
            putValueArgument(0, getThisParam)
            putValueArgument(1, irString(function.name.asString()))
            putValueArgument(2, kClassReferenceUnified(pluginContext, function.nonGenericReturnTypeOrAny(pluginContext)))
            putValueArgument(3, irCallListOf(irCallArgVarargParams(function.fullValueParameterList)))
        }
        mokkeryCall.putValueArgument(0, contextCreationCall)
        return mokkeryCall
    }


    protected fun IrClass.inheritMokkeryInterceptor(
        interceptorScopeClass: IrClass,
        classToMock: IrClass,
        interceptorInit: IrBlockBodyBuilder.(IrConstructor) -> IrCall,
        block: IrBlockBodyBuilder.(IrConstructor) -> Unit = { },
    ) {
        val mockClass = this
        val interceptor = overridePropertyBackingField(pluginContext, interceptorScopeClass.getProperty("interceptor"))
        val idProperty = overridePropertyBackingField(pluginContext, interceptorScopeClass.getProperty("id"))
        addConstructor {
            isPrimary = true
        }.apply {
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                +irDelegatingDefaultConstructorOrAny(classToMock)
                val identifierCall = irCallMokkeryClassIdentifier(pluginContext, mockClass, classToMock)
                val initializerCall = interceptorInit(this@apply)
                +irSetField(irGet(thisReceiver!!), interceptor.backingField!!, initializerCall)
                +irSetField(irGet(thisReceiver!!), idProperty.backingField!!, identifierCall)
                block(this@apply)
            }
        }
        addOverridingMethod(pluginContext, pluginContext.irBuiltIns.memberToString.owner) {
            +irReturn(irCall(idProperty.getter!!.symbol).apply {
                dispatchReceiver = irGet(it.dispatchReceiverParameter!!)
            })
        }
    }

    protected fun IrCall.checkInterceptionPossibilities(functionName: FqName) {
        val functionNameString = functionName.shortName().asString()
        val typeToMock = typeArguments.firstOrNull()
            ?.takeIf { !it.isTypeParameter() }
            ?: mokkeryError(irFile) {
                "${functionNameString.capitalizeAsciiOnly()} call must be direct! It can't be a type parameter!"
            }
        val classToMock = typeToMock.getClass()!!
        if (classToMock.modality == Modality.FINAL) mokkeryError(irFile) {
            "${functionNameString.capitalizeAsciiOnly()} type cannot be final!"
        }
        val allFunctionsOverridable = classToMock.functions.all { it.isOverridable }
        val allPropertiesOverridable = classToMock.properties.all { it.isOverridableProperty() }
        if (!allFunctionsOverridable || !allPropertiesOverridable) mokkeryError(irFile) {
            "${functionNameString.capitalizeAsciiOnly()} type must have all methods and properties overridable!"
        }
        if (classToMock.modality == Modality.SEALED) mokkeryError(irFile) { "Intercepting sealed types is not supported!" }
        if (!classToMock.isInterface && classToMock.defaultConstructor == null) mokkeryError(irFile) {
            "${functionNameString.capitalizeAsciiOnly()} type must have no-arg constructor!"
        }
        messageCollector.logAt(this, irFile) {
            "Recognized $functionNameString call with type ${typeToMock.asString()}!"
        }
    }

    private fun IrBuilderWithScope.irCallArgVarargParams(parameters: List<IrValueParameter>): IrVarargImpl {
        return irVararg(
            elementType = irClasses.CallArg.defaultType,
            values = parameters
                .map {
                    irCall(irClasses.CallArg.primaryConstructor!!).apply {
                        putValueArgument(0, irString(it.name.asString()))
                        putValueArgument(
                            1,
                            kClassReferenceUnified(pluginContext, it.nonGenericReturnTypeOrAny(pluginContext))
                        )
                        putValueArgument(2, irGet(it))
                        putValueArgument(3, irBoolean(it.isVararg))
                    }
                }
        )
    }

    private fun IrBuilderWithScope.irCallListOf(varargExpression: IrVararg): IrCall {
        val listOf = pluginContext.referenceFunctions(Kotlin.FunctionId.listOf).first {
            it.owner.valueParameters.firstOrNull()?.isVararg == true
        }
        return irCall(listOf).apply {
            putValueArgument(0, varargExpression)
        }
    }

    private fun IrBuilderWithScope.irCallMokkeryClassIdentifier(
        pluginContext: IrPluginContext,
        mockClass: IrClass,
        classToMock: IrClass
    ): IrStringConcatenation {
        return irConcat().apply {
            addArgument(irString(classToMock.kotlinFqName.asString() + "@"))
            addArgument(irToString(pluginContext, irCallHashCode(mockClass), 33))
        }
    }

    inner class IrClasses {
        val MokkerySpy = pluginContext.getClass(Mokkery.ClassId.MokkerySpy)
        val MokkerySpyScope = pluginContext.getClass(Mokkery.ClassId.MokkerySpyScope)


        val MokkeryMock = pluginContext.getClass(Mokkery.ClassId.MokkeryMock)
        val MokkeryMockScope = pluginContext.getClass(Mokkery.ClassId.MokkeryMockScope)

        val MokkeryInterceptor = pluginContext.getClass(Mokkery.ClassId.MokkeryInterceptor)
        val MokkeryInterceptorScope = pluginContext.getClass(Mokkery.ClassId.MokkeryInterceptorScope)

        val TemplatingInterceptor = pluginContext.getClass(Mokkery.ClassId.TemplatingInterceptor)

        val MockMode = pluginContext.getClass(Mokkery.ClassId.MockMode)
        val CallArg = pluginContext.getClass(Mokkery.ClassId.CallArg)
        val CallContext = pluginContext.getClass(Mokkery.ClassId.CallContext)
    }

    inner class IrFunctions {
        val MokkeryMock = pluginContext.firstFunction(Mokkery.FunctionId.MokkeryMock)
        val MokkerySpy = pluginContext.firstFunction(Mokkery.FunctionId.MokkerySpy)
    }

}
