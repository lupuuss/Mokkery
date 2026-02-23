package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.annotationSelector
import dev.mokkery.plugin.context.configuration
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.addOverridingProperty
import dev.mokkery.plugin.ir.annotations.toFilter
import dev.mokkery.plugin.ir.computeSignature
import dev.mokkery.plugin.ir.createParametersMapTo
import dev.mokkery.plugin.ir.irBuiltIns
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irFactory
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.overridableProperties
import dev.mokkery.plugin.ir.pluginContext
import dev.mokkery.plugin.ir.requireSimpleFunctionOwner
import dev.mokkery.plugin.ir.transformer.core.TransformerScope
import dev.mokkery.plugin.ir.transformer.core.addToCurrentFile
import dev.mokkery.plugin.ir.transformer.core.declarationIrBuilder
import dev.mokkery.plugin.ir.transformer.core.referenced
import dev.mokkery.plugin.ir.transformer.mock.stubs.irDelegatingConstructorWithStubs
import dev.mokkery.plugin.ir.typeWith
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

context(scope: TransformerScope)
fun buildDefaultsExtractorFactoryIfRequired(
    className: Name,
    classesToIntercept: List<IrClass>,
    bodyBuilder: IrBlockBodyBuilder,
): IrExpression? {
    val defaultsExtractor = buildDefaultsExtractorOrNull(className, classesToIntercept) ?: return null
    val defaultsFactoryClass = buildDefaultsFactoryClassWith(className, defaultsExtractor.primaryConstructor!!)
    return bodyBuilder.run {
        irCallConstructor(defaultsFactoryClass.primaryConstructor!!)
    }
}

context(scope: TransformerScope)
private fun buildDefaultsExtractorOrNull(
    className: Name,
    classesToIntercept: List<IrClass>
): IrClass? {
    val anyDefaults = classesToIntercept.any { clazz ->
        clazz.functions.any { it.parameters.any(IrValueParameter::hasDefaultValue) }
    }
    if (!anyDefaults) return null
    val throwArgumentsFunction = referenced(MokkeryIr.Function.throwArguments)
    val methodWithoutDefaultFunction = referenced(MokkeryIr.Function.methodWithoutDefaultsError)
    val irBuiltIns = irBuiltIns
    val defaultsExtractorClass = irFactory.buildClass { name = Name.identifier("DE${className.asString()}") }
    classesToIntercept.forEach(defaultsExtractorClass::copyTypeParametersFrom)
    defaultsExtractorClass.addToCurrentFile()
    defaultsExtractorClass.createThisReceiverParameter()
    defaultsExtractorClass.origin = MokkeryIr.Origin
    val parameterMap = classesToIntercept.createParametersMapTo(defaultsExtractorClass)
    val mockedTypes = classesToIntercept.typeWith(parameterMap)
    defaultsExtractorClass.superTypes = mockedTypes
    if (classesToIntercept.all(IrClass::isInterface)) {
        defaultsExtractorClass.superTypes += irBuiltIns.anyType
    }
    defaultsExtractorClass.addDefaultsExtractorConstructor(classesToIntercept)
    val annotationFilter = configuration.annotationSelector.toFilter()
    classesToIntercept.flatMap { it.overridableFunctions }
        .groupBy(IrDeclaration::computeSignature)
        .map { (_, functions) ->
            defaultsExtractorClass.addOverridingMethod(
                context = pluginContext,
                functions = functions,
                parameterMap = parameterMap,
                annotationFilter = annotationFilter,
            ) { newFunc ->
                newFunc.returnType = irBuiltIns.nothingType
                if (functions.any { func -> func.parameters.any(IrValueParameter::hasDefaultValue) }) {
                    +irCall(throwArgumentsFunction) {
                        arguments[0] = irVararg(
                            elementType = irBuiltIns.anyNType,
                            elements = newFunc.parameters.map { irGet(it) }
                        )
                    }
                } else {
                    +irCall(methodWithoutDefaultFunction)
                }
            }
        }
    classesToIntercept.flatMap { it.overridableProperties }
        .groupBy(IrDeclaration::computeSignature)
        .map { (_, properties) ->
            defaultsExtractorClass.addOverridingProperty(
                context = pluginContext,
                properties = properties,
                parameterMap = parameterMap,
                annotationFilter = annotationFilter,
                getterBlock = { +irCall(methodWithoutDefaultFunction) },
                setterBlock = { +irCall(methodWithoutDefaultFunction) }
            )
        }
    return defaultsExtractorClass
}

context(scope: TransformerScope)
private fun buildDefaultsFactoryClassWith(className: Name, constructor: IrConstructor): IrClass {
    val defaultsExtractorFactoryInterface = referenced(MokkeryIr.Class.DefaultsExtractorFactory)
    val defaultsExtractorFactoryClassImpl = irFactory.buildClass {
        name = Name.identifier("DEF${className.asString()}")
    }
    defaultsExtractorFactoryClassImpl.superTypes = listOf(
        defaultsExtractorFactoryInterface.defaultType,
        irBuiltIns.anyType
    )
    defaultsExtractorFactoryClassImpl.createThisReceiverParameter()
    defaultsExtractorFactoryClassImpl.addConstructor { isPrimary = true }.apply {
        body = symbol.declarationIrBuilder.irBlockBody {
            +irDelegatingConstructorCall(irBuiltIns.anyClass.owner.primaryConstructor!!)
        }
    }
    defaultsExtractorFactoryClassImpl.addOverridingMethod(
        context = pluginContext,
        function = defaultsExtractorFactoryInterface.requireSimpleFunctionOwner("createDefaultsExtractor"),
        block = { +irReturn(irCallConstructor(constructor)) }
    )
    defaultsExtractorFactoryClassImpl.addToCurrentFile()
    return defaultsExtractorFactoryClassImpl
}

context(scope: TransformerScope)
private fun IrClass.addDefaultsExtractorConstructor(
    classesToIntercept: List<IrClass>,
) {
    addConstructor {
        isPrimary = true
    }.apply {
        body = symbol.declarationIrBuilder.irBlockBody {
            +irDelegatingConstructorWithStubs(classesToIntercept.firstOrNull { it.isClass })
        }
    }
}
