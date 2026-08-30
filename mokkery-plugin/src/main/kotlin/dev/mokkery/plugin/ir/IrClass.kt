package dev.mokkery.plugin.ir

import dev.mokkery.plugin.ir.annotations.AnnotationFilter
import dev.mokkery.plugin.ir.annotations.deepApplyAnnotationsFilter
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.addSetter
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.typeWithParameters
import org.jetbrains.kotlin.ir.util.copyAnnotationsFrom
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.createDispatchReceiverParameterWithClassParent
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.fields
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isMethodOfAny
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.memoryOptimizedFlatMap
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.memoryOptimizedZip

fun IrClass.requirePropertyOwner(name: String): IrProperty {
    val nameId = Name.identifier(name)
    return properties.first { it.name == nameId }
}

fun IrClass.requireFieldOwner(name: String): IrField {
    val nameId = Name.identifier(name)
    return fields.first { it.name == nameId }
}

fun IrClass.requirePropertyGetterOwner(name: String): IrSimpleFunction = requirePropertyOwner(name).getter!!

fun IrClass.requireSimpleFunction(name: String): IrSimpleFunctionSymbol {
    return getSimpleFunction(name)!!
}

fun IrClass.requireSimpleFunctionOwner(name: String): IrSimpleFunction = requireSimpleFunction(name).owner

fun IrClass.getEnumEntry(name: String): IrEnumEntry {
    return declarations
        .filterIsInstance<IrEnumEntry>()
        .first { it.name == Name.identifier(name) }
}

fun List<IrType>.forEachIndexedTypeArgument(block: (Int, IrType?) -> Unit) {
    flattenArgumentTypes().forEachIndexed { index, it -> block(index, it?.eraseTypeParameters()) }
}

val IrClass.erasedTypeArguments: List<IrType> get() = typeArgumentsFrom(emptyList())

fun IrClass.typeSubstitutionForSuperClass(superClass: IrClass): Map<IrTypeParameterSymbol, IrType>? {
    if (this == superClass) return emptyMap()
    superTypes.forEach { superType ->
        val currentClass = superType.classOrNull?.owner ?: return@forEach
        val currentSubstitution = superType.typeSubstitution
        if (currentClass == superClass) return currentSubstitution
        val substitution = currentClass.typeSubstitutionForSuperClass(superClass) ?: return@forEach
        return substitution.mapValues { (_, type) -> type.substitute(currentSubstitution) }
    }
    return null
}

fun IrClass.addOverridingMethod(
    context: IrGeneratorContext,
    function: IrSimpleFunction,
    annotationFilter: AnnotationFilter = AnnotationFilter.all,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = emptyMap(),
    block: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit
) = addOverridingMethod(context, listOf(function), parameterMap, annotationFilter, block)

fun IrClass.addOverridingMethod(
    context: IrGeneratorContext,
    functions: List<IrSimpleFunction>,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = emptyMap(),
    annotationFilter: AnnotationFilter = AnnotationFilter.all,
    block: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit
): IrSimpleFunction {
    val function = functions.first()
    return addFunction {
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
        deepApplyAnnotationsFilter(annotationFilter)
        metadata = function.metadata
        body = DeclarationIrBuilder(context, symbol)
            .irBlockBody { block(this@apply) }
    }
}

fun IrClass.overrideAllOverridableFunctions(
    context: IrGeneratorContext,
    superClass: IrClass,
    annotationFilter: AnnotationFilter = AnnotationFilter.all,
    override: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
) {
    superClass
        .overridableFunctions
        .forEach { overridableFun ->
            addOverridingMethod(
                context = context,
                function = overridableFun,
                annotationFilter = annotationFilter,
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
    annotationFilter: AnnotationFilter = AnnotationFilter.all,
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
                annotationFilter = annotationFilter,
                getterBlock = getterBlock,
                setterBlock = setterBlock
            )
        }
}

fun IrClass.addOverridingProperty(
    context: IrGeneratorContext,
    property: IrProperty,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = emptyMap(),
    annotationFilter: AnnotationFilter = AnnotationFilter.all,
    getterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
    setterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
) = addOverridingProperty(context, listOf(property), parameterMap, annotationFilter, getterBlock, setterBlock)

fun IrClass.addOverridingProperty(
    context: IrGeneratorContext,
    properties: List<IrProperty>,
    parameterMap: Map<IrTypeParameter, IrTypeParameter> = emptyMap(),
    annotationFilter: AnnotationFilter = AnnotationFilter.all,
    getterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
    setterBlock: IrBlockBodyBuilder.(IrSimpleFunction) -> Unit,
): IrProperty {
    val property = properties.first()
    return addProperty {
        updateFrom(property)
        name = property.name
        modality = Modality.FINAL
        origin = IrDeclarationOrigin.DEFINED
        isFakeOverride = false
        isVar = properties.any(IrProperty::isVar)
    }.apply {
        overriddenSymbols = property.overriddenSymbols + properties.map(IrProperty::symbol)
        val baseGetter = properties.firstNotNullOfOrNull(IrProperty::getter)
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
            getter.deepApplyAnnotationsFilter(annotationFilter)
            getter.body = DeclarationIrBuilder(context, getter.symbol).irBlockBody { getterBlock(getter) }
        }
        val baseSetter = properties.firstNotNullOfOrNull(IrProperty::setter)
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
            setter.deepApplyAnnotationsFilter(annotationFilter)
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
        addDefaultGetter(context).overriddenSymbols = listOf(property.getter!!.symbol)
        if (property.isVar) addDefaultSetter(context).overriddenSymbols = listOf(property.setter!!.symbol)
    }
}

fun IrProperty.addDefaultGetter(context: IrGeneratorContext): IrSimpleFunction {
    val backingField = backingField!!
    return addGetter {
        this.returnType = backingField.type
        origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.apply {
        parameters = listOf(createDispatchReceiverParameterWithClassParent())
        body = DeclarationIrBuilder(context, symbol).irBlockBody {
            +irReturn(irGetField(irGet(parameters[0]), backingField))
        }
    }
}

fun IrProperty.addDefaultSetter(context: IrGeneratorContext): IrSimpleFunction {
    val backingField = backingField!!
    isVar = true
    return addSetter {
        this.returnType = context.irBuiltIns.unitType
        origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
    }.apply {
        parameters = listOf(
            createDispatchReceiverParameterWithClassParent(),
            buildValueParameter(this) {
                this.type = backingField.type
                this.kind = IrParameterKind.Regular
                this.name = Name.identifier("value")
            }
        )
        body = DeclarationIrBuilder(context, symbol).irBlockBody {
            +irSetField(irGet(parameters[0]), backingField, irGet(parameters[1]))
        }
    }
}

val IrClass.overridableFunctions
    get() = functions.filter { it.isOverridable && !it.isMethodOfAny() }

val IrClass.overridableProperties
    get() = properties.filter { it.isOverridable }

val IrClass.defaultTypeErased get() = defaultType.eraseTypeParameters()

fun List<IrClass>.createParametersMapTo(cls: IrClass): Map<IrTypeParameter, IrTypeParameter> {
    return memoryOptimizedFlatMap { it.typeParameters }
        .memoryOptimizedZip(cls.typeParameters)
        .toMap()
}

fun List<IrClass>.typeWith(parameterMap: Map<IrTypeParameter, IrTypeParameter>): List<IrType> {
    return memoryOptimizedMap {
        it.symbol.typeWithParameters(it.typeParameters.memoryOptimizedMap(parameterMap::getValue))
    }
}
