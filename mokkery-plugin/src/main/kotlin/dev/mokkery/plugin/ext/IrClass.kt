package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.buildReceiverParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.name.Name

fun IrClass.getProperty(name: String): IrProperty {
    val nameId = Name.identifier(name)
    return properties.first { it.name == nameId }
}

fun IrClass.buildThisValueParam() = buildReceiverParameter(
    parent = this,
    origin = IrDeclarationOrigin.INSTANCE_RECEIVER,
    type = symbol.typeWithParameters(typeParameters)
)

fun IrClass.addOverridingMethod(
    context: IrGeneratorContext,
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
        overriddenSymbols = function.overriddenSymbols + function.symbol
        typeParameters = function.typeParameters
        valueParameters = function.valueParameters
        dispatchReceiverParameter = buildThisValueParam()
        extensionReceiverParameter = function.extensionReceiverParameter
        contextReceiverParametersCount = function.contextReceiverParametersCount
        body = DeclarationIrBuilder(context, symbol)
            .irBlockBody { block(this@apply) }
    }
}

fun IrClass.addOverridingProperty(
    context: IrGeneratorContext,
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
        overriddenSymbols = property.overriddenSymbols + property.symbol
        setDeclarationsParent(this@addOverridingProperty)
        addGetter().also {
            it.returnType = property.getter!!.returnType
            it.dispatchReceiverParameter = buildThisValueParam()
            it.body = DeclarationIrBuilder(context, it.symbol).irBlockBody { getterBlock(it) }
        }
        getter?.overriddenSymbols = listOf(property.getter!!.symbol)
        if (property.isVar) {
            addSetter().also {
                it.returnType = property.setter!!.returnType
                it.dispatchReceiverParameter = buildThisValueParam()
                it.body = DeclarationIrBuilder(context, it.symbol).irBlockBody { setterBlock(it) }
            }
            setter?.overriddenSymbols = listOf(property.setter!!.symbol)
        }
    }
}
