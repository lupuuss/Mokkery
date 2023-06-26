package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
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
import org.jetbrains.kotlin.ir.overrides.isOverridableProperty
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isMethodOfAny
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.name.Name

fun IrClass.getProperty(name: String): IrProperty {
    val nameId = Name.identifier(name)
    return properties.first { it.name == nameId }
}

fun IrClass.getEnumEntry(name: String): IrEnumEntry {
    return declarations.filterIsInstance<IrEnumEntry>()
        .first{ it.name == Name.identifier(name) }
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
        valueParameters = function.valueParameters.map { it.copyTo(this).apply { defaultValue = null } }
        dispatchReceiverParameter = buildThisValueParam()
        extensionReceiverParameter = function.extensionReceiverParameter?.copyTo(this)
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
            it.overriddenSymbols = listOf(property.getter!!.symbol)
            it.returnType = property.getter!!.returnType
            it.dispatchReceiverParameter = buildThisValueParam()
            it.extensionReceiverParameter = property.getter!!.extensionReceiverParameter?.copyTo(it)
            it.body = DeclarationIrBuilder(context, it.symbol).irBlockBody { getterBlock(it) }
        }
        if (property.isVar) {
            addSetter().also { setter ->
                setter.returnType = property.setter!!.returnType
                setter.dispatchReceiverParameter = buildThisValueParam()
                setter.valueParameters = property.setter!!.valueParameters.map { it.copyTo(setter) }
                setter.overriddenSymbols = listOf(property.setter!!.symbol)
                setter.extensionReceiverParameter = property.setter!!.extensionReceiverParameter?.copyTo(setter)
                setter.body = DeclarationIrBuilder(context, setter.symbol).irBlockBody { setterBlock(setter) }
            }
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
        }
        overriddenSymbols = listOf(property.symbol)
        addDefaultGetter(this@overridePropertyBackingField, context.irBuiltIns)
        getter?.overriddenSymbols = listOf(property.getter!!.symbol)
    }
}

val IrClass.overridableFunctions
    get() = functions.filter { it.isOverridable && !it.isMethodOfAny() }

val IrClass.overridableProperties
    get() = properties.filter { it.isOverridableProperty() }

fun IrClass.createUniqueMockName(type: String) = kotlinFqName
    .asString()
    .replace(".", "_")
    .plus("${type}ByMokkery")
    .let(Name::identifier)
