package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.jvm.ir.eraseTypeParameters
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.addDefaultGetter
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.buildReceiverParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.copyAnnotationsFrom
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isMethodOfAny
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name

fun IrClass.getProperty(name: String): IrProperty {
    val nameId = Name.identifier(name)
    return properties.first { it.name == nameId }
}

fun IrClass.getEnumEntry(name: String): IrEnumEntry {
    return declarations
        .filterIsInstance<IrEnumEntry>()
        .first { it.name == Name.identifier(name) }
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
) = addOverridingMethod(context, listOf(function), block)

fun IrClass.addOverridingMethod(
    context: IrGeneratorContext,
    functions: List<IrSimpleFunction>,
    block: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit
) {
    val function = functions.first()
    addFunction {
        updateFrom(function)
        name = function.name
        modality = Modality.FINAL
        origin = IrDeclarationOrigin.DEFINED
        isFakeOverride = false
    }.apply {
        overriddenSymbols = function.overriddenSymbols + functions.map(IrSimpleFunction::symbol)
        metadata = function.metadata
        dispatchReceiverParameter = buildThisValueParam()
        copyTypeParametersFrom(function)
        copyAnnotationsFrom(function)
        copyReturnTypeFrom(function)
        copyParametersFrom(function)
        contextReceiverParametersCount = function.contextReceiverParametersCount
        body = DeclarationIrBuilder(context, symbol)
            .irBlockBody { block(this@apply) }
    }
}

fun IrClass.overrideAllOverridableFunctions(
    context: IrGeneratorContext,
    superClass: IrClass,
    override: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
) {
    superClass
        .overridableFunctions
        .forEach { overridableFun ->
            addOverridingMethod(context, overridableFun) {
                override(it)
            }
        }
}

fun IrClass.overrideAllOverridableProperties(
    context: IrGeneratorContext,
    superClass: IrClass,
    getterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
    setterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
) {
    superClass
        .overridableProperties
        .forEach { property ->
            addOverridingProperty(context, property, getterBlock, setterBlock)
        }
}

fun IrClass.addOverridingProperty(
    context: IrGeneratorContext,
    property: IrProperty,
    getterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
    setterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
) = addOverridingProperty(context, listOf(property), getterBlock, setterBlock)

fun IrClass.addOverridingProperty(
    context: IrGeneratorContext,
    properties: List<IrProperty>,
    getterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
    setterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
) {
    val property = properties.first()
    addProperty {
        updateFrom(property)
        name = property.name
        modality = Modality.FINAL
        origin = IrDeclarationOrigin.DEFINED
        isFakeOverride = false
    }.apply {
        overriddenSymbols = property.overriddenSymbols + properties.map(IrProperty::symbol)
        val baseGetter = property.getter!!
        val getter = addGetter()
        getter.overriddenSymbols = properties.mapNotNull { it.getter?.symbol }
        getter.metadata = baseGetter.metadata
        getter.dispatchReceiverParameter = buildThisValueParam()
        getter.contextReceiverParametersCount = baseGetter.contextReceiverParametersCount
        getter.copyTypeParametersFrom(baseGetter)
        getter.copyReturnTypeFrom(baseGetter)
        getter.copyParametersFrom(baseGetter)
        getter.copyAnnotationsFrom(baseGetter)
        getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody { getterBlock(getter) }
        if (property.isVar) {
            val baseSetter = property.setter!!
            val setter = addSetter()
            setter.metadata = baseSetter.metadata
            setter.dispatchReceiverParameter = buildThisValueParam()
            setter.contextReceiverParametersCount = baseSetter.contextReceiverParametersCount
            setter.copyTypeParametersFrom(baseSetter)
            setter.copyReturnTypeFrom(baseSetter)
            setter.copyParametersFrom(baseSetter)
            setter.copyAnnotationsFrom(baseSetter)
            setter.overriddenSymbols = properties.mapNotNull { it.setter?.symbol }
            setter.body = DeclarationIrBuilder(context, setter.symbol).irBlockBody { setterBlock(setter) }
        }
    }
}

fun IrClass.overridePropertyBackingField(context: IrGeneratorContext, property: IrProperty): IrProperty {
    return addProperty {
        name = property.name
        isVar = property.isVar
        modality = Modality.FINAL
        origin = IrDeclarationOrigin.DEFINED
    }.apply {
        addBackingField {
            type = property.getter!!.returnType
            visibility = DescriptorVisibilities.PRIVATE
        }
        overriddenSymbols = listOf(property.symbol)
        addDefaultGetter(this@overridePropertyBackingField, context.irBuiltIns)
        getter?.overriddenSymbols = listOf(property.getter!!.symbol)
    }
}

val IrClass.overridableFunctions
    get() = functions.filter { it.isOverridable && !it.isMethodOfAny() }

val IrClass.overridableProperties
    get() = properties.filter { it.isOverridable }

val IrClass.defaultTypeErased get() = defaultType.eraseTypeParameters()
