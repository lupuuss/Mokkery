package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.getProperty
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irDelegatingDefaultConstructorOrAny
import dev.mokkery.plugin.ir.irSetPropertyField
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.overridePropertyBackingField
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.kotlinFqName

fun IrClass.inheritMokkeryInterceptor(
    transformer: TransformerScope,
    interceptorScopeClass: IrClass,
    classToIntercept: IrClass,
    interceptorInit: IrBlockBodyBuilder.(IrConstructor) -> IrCall,
    block: IrBlockBodyBuilder.(IrConstructor) -> Unit = { },
) {
    inheritMokkeryInterceptor(
        transformer = transformer,
        interceptorScopeClass = interceptorScopeClass,
        classesToIntercept = listOf(classToIntercept),
        typeName = classToIntercept.kotlinFqName.asString(),
        interceptorInit = interceptorInit,
        block = block
    )
}

fun IrClass.inheritMokkeryInterceptor(
    transformer: TransformerScope,
    interceptorScopeClass: IrClass,
    classesToIntercept: List<IrClass>,
    typeName: String,
    interceptorInit: IrBlockBodyBuilder.(IrConstructor) -> IrCall,
    block: IrBlockBodyBuilder.(IrConstructor) -> Unit = { },
) {
    val context = transformer.pluginContext
    val interceptor = overridePropertyBackingField(context, interceptorScopeClass.getProperty("interceptor"))
    val idProperty = overridePropertyBackingField(context, interceptorScopeClass.getProperty("id"))
    val typeProperty = overridePropertyBackingField(context, interceptorScopeClass.getProperty("interceptedTypes"))
    addConstructor {
        isPrimary = true
    }.apply {
        body = DeclarationIrBuilder(context, symbol).irBlockBody {
            +irDelegatingDefaultConstructorOrAny(classesToIntercept.firstOrNull { it.isClass })
            val initializerCall = interceptorInit(this@apply)
            val identifierCall = irCall(transformer.getFunction(Mokkery.Function.generateMockId)) {
                putValueArgument(0, irString(typeName))
            }
            +irSetPropertyField(
                thisParam = thisReceiver!!,
                property = typeProperty,
                value = irCallListOf(
                    transformerScope = transformer,
                    type = context.irBuiltIns.kClassClass.defaultType,
                    expressions = classesToIntercept.map { kClassReference(it.defaultTypeErased) }
                )
            )
            +irSetPropertyField(thisReceiver!!, interceptor, initializerCall)
            +irSetPropertyField(thisReceiver!!, idProperty, identifierCall)
            block(this@apply)
        }
    }
    addOverridingMethod(context, context.irBuiltIns.memberToString.owner) {
        +irReturn(irCall(idProperty.getter!!.symbol) {
            dispatchReceiver = irGet(it.dispatchReceiverParameter!!)
        })
    }
}
