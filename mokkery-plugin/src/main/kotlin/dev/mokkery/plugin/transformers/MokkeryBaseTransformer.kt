package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ext.addOverridingMethod
import dev.mokkery.plugin.ext.firstFunction
import dev.mokkery.plugin.ext.getClass
import dev.mokkery.plugin.ext.getProperty
import dev.mokkery.plugin.ext.irAnyVarargParams
import dev.mokkery.plugin.ext.irCallMokkeryClassIdentifier
import dev.mokkery.plugin.ext.kClassReferenceUnified
import dev.mokkery.plugin.ext.mokkerySignature
import dev.mokkery.plugin.ext.nonGenericReturnTypeOrAny
import dev.mokkery.plugin.ext.overridePropertyBackingField
import dev.mokkery.plugin.infoAt
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
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.overrides.isOverridableProperty
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.isVararg
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

    inner class IrClasses {
        val MokkerySpy = pluginContext.getClass(Mokkery.ClassId.MokkerySpy)
        val MokkerySpyScope = pluginContext.getClass(Mokkery.ClassId.MokkerySpyScope)


        val MokkeryMock = pluginContext.getClass(Mokkery.ClassId.MokkeryMock)
        val MokkeryMockScope = pluginContext.getClass(Mokkery.ClassId.MokkeryMockScope)

        val MokkeryInterceptor = pluginContext.getClass(Mokkery.ClassId.MokkeryInterceptor)
        val MokkeryInterceptorScope = pluginContext.getClass(Mokkery.ClassId.MokkeryInterceptorScope)

        val TemplatingInterceptor = pluginContext.getClass(Mokkery.ClassId.TemplatingInterceptor)

        val MockMode = pluginContext.getClass(Mokkery.ClassId.MockMode)
    }

    inner class IrFunctions {
        val MokkeryMock = pluginContext.firstFunction(Mokkery.FunctionId.MokkeryMock)
        val MokkerySpy = pluginContext.firstFunction(Mokkery.FunctionId.MokkerySpy)
    }

    protected fun IrBlockBodyBuilder.irCallInterceptingMethod(function: IrSimpleFunction): IrCall {
        val thisParam = irGet(function.dispatchReceiverParameter!!)
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
                dispatchReceiver = thisParam
            }
        mokkeryCall.putValueArgument(0, irString(function.mokkerySignature))
        mokkeryCall.putValueArgument(
            index = 1,
            valueArgument = kClassReferenceUnified(pluginContext, function.nonGenericReturnTypeOrAny(pluginContext))
        )
        mokkeryCall.putValueArgument(2, irInt(function.valueParameters.indexOfFirst { it.isVararg }))
        mokkeryCall.putValueArgument(3, irAnyVarargParams(function.fullValueParameterList))
        return mokkeryCall
    }


    protected fun IrClass.inheritMokkeryInterceptor(
        interceptorScopeClass: IrClass,
        classToMock: IrClass,
        interceptorInit: IrBlockBodyBuilder.(IrConstructor) -> IrCall,
        block: IrBlockBodyBuilder.(IrConstructor) -> Unit = { },
    ) {
        val interceptor = overridePropertyBackingField(pluginContext, interceptorScopeClass.getProperty("interceptor"))
        val idProperty = overridePropertyBackingField(pluginContext, interceptorScopeClass.getProperty("id"))
        addConstructor {
            isPrimary = true
        }.apply {
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                +irDelegatingConstructorCall(
                    classToMock.defaultConstructor ?: pluginContext.irBuiltIns.anyClass.owner.primaryConstructor!!
                )
                val id = createTmpVariable(irCallMokkeryClassIdentifier(this@inheritMokkeryInterceptor, classToMock))
                val initializerCall = interceptorInit(this@apply)
                initializerCall.putValueArgument(0, irGet(id))
                +irSetField(irGet(thisReceiver!!), interceptor.backingField!!, initializerCall)
                +irSetField(irGet(thisReceiver!!), idProperty.backingField!!, irGet(id))
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
        messageCollector.infoAt(this, irFile) {
            "Recognized $functionNameString call with type ${typeToMock.asString()}!"
        }
    }
}
