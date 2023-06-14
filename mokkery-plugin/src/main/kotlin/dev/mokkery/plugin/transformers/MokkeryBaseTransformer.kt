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
import dev.mokkery.plugin.ext.overridePropertyWithBackingField
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

abstract class MokkeryBaseTransformer(
    protected val pluginContext: IrPluginContext,
) : IrElementTransformerVoid() {

    protected val irClasses = IrClasses()
    protected val irFunctions = IrFunctions()

    inner class IrClasses {
        val MokkeryMock = pluginContext.getClass(Mokkery.ClassId.MokkeryMock)
        val MokkeryMockScope = pluginContext.getClass(Mokkery.ClassId.MokkeryMockScope)
        val MokkeryInterceptor = pluginContext.getClass(Mokkery.ClassId.MokkeryInterceptor)
        val MokkeryInterceptorScope = pluginContext.getClass(Mokkery.ClassId.MokkeryInterceptorScope)
        val MockMode = pluginContext.getClass(Mokkery.ClassId.MockMode)
    }
    inner class IrFunctions {
        val MokkeryMock = pluginContext.firstFunction(Mokkery.FunctionId.MokkeryMock)
    }

    protected fun IrBuilderWithScope.irCallMockModeDefault(): IrFunctionAccessExpression {
        val companion = irClasses.MockMode.companionObject()!!
        return irCall(companion.getPropertyGetter("Default")!!.owner).apply {
            dispatchReceiver = irGetObject(companion.symbol)
        }
    }

    protected fun IrBlockBodyBuilder.irCallInterceptingMethod(function: IrSimpleFunction): IrCall {
        val thisParam = irGet(function.dispatchReceiverParameter!!)
        val mokkeryCall = if (function.isSuspend) {
            irCall(irClasses.MokkeryInterceptor.symbol.functionByName("interceptSuspendCall"))
        } else {
            irCall(irClasses.MokkeryInterceptor.symbol.functionByName("interceptCall"))
        }
        mokkeryCall.dispatchReceiver = irCall(irClasses.MokkeryInterceptorScope.getPropertyGetter("interceptor")!!).apply {
            dispatchReceiver = thisParam
        }
        mokkeryCall.putValueArgument(0, irString(function.mokkerySignature))
        mokkeryCall.putValueArgument(
            index = 1,
            valueArgument = kClassReferenceUnified(pluginContext, function.nonGenericReturnTypeOrAny(pluginContext))
        )
        mokkeryCall.putValueArgument(2, irAnyVarargParams(function.valueParameters))
        return mokkeryCall
    }


    fun IrClass.inheritMokkeryInterceptor(
        interceptorScopeClass: IrClass,
        classToMock: IrClass,
        initializer: IrBuilderWithScope.(IrConstructor) -> IrCall
    ) {
        val interceptorProperty = overridePropertyWithBackingField(pluginContext, interceptorScopeClass.getProperty("interceptor"))
        val idProperty = overridePropertyWithBackingField(pluginContext, interceptorScopeClass.getProperty("id"))
        addConstructor {
            isPrimary = true
        }.apply {
            body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                +irDelegatingConstructorCall(pluginContext.irBuiltIns.anyClass.owner.primaryConstructor!!)
                val id = createTmpVariable(irCallMokkeryClassIdentifier(this@inheritMokkeryInterceptor, classToMock))
                val initializerCall = initializer(this@apply)
                initializerCall.putValueArgument(0, irGet(id))
                +irSetField(irGet(thisReceiver!!), interceptorProperty.backingField!!, initializerCall)
                +irSetField(irGet(thisReceiver!!), idProperty.backingField!!, irGet(id))
            }
        }
        addOverridingMethod(pluginContext, pluginContext.irBuiltIns.memberToString.owner) {
            +irReturn(irCall(idProperty.getter!!.symbol).apply {
                dispatchReceiver = irGet(it.dispatchReceiverParameter!!)
            })
        }
    }
}
