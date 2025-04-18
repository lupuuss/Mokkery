package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.copyAnnotationsFrom
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.createDispatchReceiverParameterWithClassParent
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isMethodOfAny
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.memoryOptimizedFlatMap

fun IrClass.getProperty(name: String): IrProperty {
    val nameId = Name.identifier(name)
    return properties.first { it.name == nameId }
}

fun IrClass.getEnumEntry(name: String): IrEnumEntry {
    return declarations
        .filterIsInstance<IrEnumEntry>()
        .first { it.name == Name.identifier(name) }
}

fun IrType.forEachIndexedTypeArgument(block: (Int, IrType?) -> Unit) {
    (this as? IrSimpleType)
        ?.arguments
        ?.forEachIndexed { index, it ->
            block(index, it.typeOrNull?.eraseTypeParameters())
        }
}

fun List<IrType>.forEachIndexedTypeArgument(block: (Int, IrType?) -> Unit) {
    memoryOptimizedFlatMap { (it as? IrSimpleType)?.arguments.orEmpty() }
        .forEachIndexed { index, it -> block(index, it.typeOrNull?.eraseTypeParameters()) }
}

fun IrClass.addOverridingMethod(
    context: IrGeneratorContext,
    function: IrSimpleFunction,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = emptyMap(),
    block: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit
) = addOverridingMethod(context, listOf(function), parameterMap, block)

fun IrClass.addOverridingMethod(
    context: IrGeneratorContext,
    functions: List<IrSimpleFunction>,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = emptyMap(),
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
        copyTypeParametersFrom(function, parameterMap = parameterMap)
        copyAnnotationsFrom(function)
        copyReturnTypeFrom(function, parameterMap)
        parameters = listOf(createDispatchReceiverParameterWithClassParent())
        copyNonDispatchParametersWithoutDefaultsFrom(function, parameterMap)
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
            addOverridingMethod(
                context = context,
                function = overridableFun,
                parameterMap = superClass.typeParameters.zip(typeParameters).toMap(),
                block = override
            )
        }
}

fun IrType.indexIfParameterOrNull(parent: IrTypeParametersContainer): Int? {
    val param = asTypeParamOrNull() ?: return null
    return param.index.takeIf { parent.typeParameters.getOrNull(param.index) == param }
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
            addOverridingProperty(
                context = context,
                property = property,
                parameterMap = superClass.typeParameters.zip(typeParameters).toMap(),
                getterBlock = getterBlock,
                setterBlock = setterBlock
            )
        }
}

fun IrClass.addOverridingProperty(
    context: IrGeneratorContext,
    property: IrProperty,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = emptyMap(),
    getterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
    setterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
) = addOverridingProperty(context, listOf(property), parameterMap, getterBlock, setterBlock)

fun IrClass.addOverridingProperty(
    context: IrGeneratorContext,
    properties: List<IrProperty>,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = emptyMap(),
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
        val baseGetter = property.getter
        if (baseGetter != null) {
            val getter = addGetter()
            getter.overriddenSymbols = properties
                .memoryOptimizedFlatMap { it.getter?.overriddenSymbols.orEmpty() + listOfNotNull(it.getter?.symbol) }
            getter.metadata = baseGetter.metadata
            getter.copyTypeParametersFrom(baseGetter, parameterMap = parameterMap)
            getter.copyReturnTypeFrom(baseGetter, parameterMap)
            getter.parameters = listOf(getter.createDispatchReceiverParameterWithClassParent())
            getter.copyNonDispatchParametersWithoutDefaultsFrom(baseGetter, parameterMap)
            getter.copyAnnotationsFrom(baseGetter)
            getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody { getterBlock(getter) }
        }
        val baseSetter = property.setter
        if (baseSetter != null) {
            val setter = addSetter()
            setter.metadata = baseSetter.metadata
            setter.copyTypeParametersFrom(baseSetter, parameterMap = parameterMap)
            setter.copyReturnTypeFrom(baseSetter, parameterMap)
            setter.parameters = listOf(setter.createDispatchReceiverParameterWithClassParent())
            setter.copyNonDispatchParametersWithoutDefaultsFrom(baseSetter, parameterMap)
            setter.copyAnnotationsFrom(baseSetter)
            setter.overriddenSymbols = properties
                .memoryOptimizedFlatMap { it.setter?.overriddenSymbols.orEmpty() + listOfNotNull(it.setter?.symbol) }
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
        val returnType = property.getter!!.returnType
        addBackingField {
            type = returnType
            visibility = DescriptorVisibilities.PRIVATE
        }
        overriddenSymbols = listOf(property.symbol)
        addGetter {
            this.returnType = returnType
            origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
        }.apply {
            parameters = listOf(createDispatchReceiverParameterWithClassParent())
            body = DeclarationIrBuilder(context, symbol).irBlockBody {
                +irReturn(irGetField(irGet(parameters[0]), backingField!!))
            }
        }
        getter?.overriddenSymbols = listOf(property.getter!!.symbol)
    }
}

val IrClass.overridableFunctions
    get() = functions.filter { it.isOverridable && !it.isMethodOfAny() }

val IrClass.overridableProperties
    get() = properties.filter { it.isOverridable }

val IrClass.defaultTypeErased get() = defaultType.eraseTypeParameters()
