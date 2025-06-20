package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.addOverridingMethod
import dev.mokkery.plugin.ir.addOverridingProperty
import dev.mokkery.plugin.ir.computeSignature
import dev.mokkery.plugin.ir.createParametersMapTo
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irDelegatingDefaultConstructorOrAny
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.overridableFunctions
import dev.mokkery.plugin.ir.overridableProperties
import dev.mokkery.plugin.ir.typeWith
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
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
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.copyTypeParametersFrom
import org.jetbrains.kotlin.ir.util.createThisReceiverParameter
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name

fun TransformerScope.buildDefaultsExtractorFactoryIfRequired(
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

private fun TransformerScope.buildDefaultsExtractorOrNull(
    className: Name,
    classesToIntercept: List<IrClass>
): IrClass? {
    val anyDefaults = classesToIntercept.any { clazz ->
        clazz.functions.any { it.parameters.any(IrValueParameter::hasDefaultValue) }
    }
    if (!anyDefaults) return null
    val throwArgumentsFunction = getFunction(Mokkery.Function.throwArguments)
    val methodWithoutDefaultFunction = getFunction(Mokkery.Function.methodWithoutDefaultsError)
    val irBuiltIns = pluginContext.irBuiltIns
    val defaultsExtractorClass = pluginContext.irFactory
        .buildClass { name = Name.identifier("DE${className.asString()}") }
    classesToIntercept.forEach(defaultsExtractorClass::copyTypeParametersFrom)
    defaultsExtractorClass.createThisReceiverParameter()
    defaultsExtractorClass.origin = Mokkery.Origin
    val parameterMap = classesToIntercept.createParametersMapTo(defaultsExtractorClass)
    val mockedTypes = classesToIntercept.typeWith(parameterMap)
    defaultsExtractorClass.superTypes = mockedTypes
    if (classesToIntercept.all(IrClass::isInterface)) {
        defaultsExtractorClass.superTypes += pluginContext.irBuiltIns.anyType
    }
    defaultsExtractorClass.addDefaultsExtractorConstructor(this, classesToIntercept)
    classesToIntercept.flatMap { it.overridableFunctions }
        .groupBy(IrDeclaration::computeSignature)
        .map { (_, functions) ->
            defaultsExtractorClass.addOverridingMethod(pluginContext, functions, parameterMap) { newFunc ->
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
                getterBlock = { +irCall(methodWithoutDefaultFunction) },
                setterBlock = { +irCall(methodWithoutDefaultFunction) }
            )
        }
    currentFile.addChild(defaultsExtractorClass)
    return defaultsExtractorClass
}

private fun IrBlockBodyBuilder.createDefaultsExtractorBody() {

}

private fun TransformerScope.buildDefaultsFactoryClassWith(className: Name, constructor: IrConstructor): IrClass {
    val irBuiltIns = pluginContext.irBuiltIns
    val defaultsExtractorFactoryInterface = getClass(Mokkery.Class.DefaultsExtractorFactory)
    val defaultsExtractorFactoryClassImpl = irBuiltIns.irFactory.buildClass {
        name = Name.identifier("DEF${className.asString()}")
    }
    defaultsExtractorFactoryClassImpl.superTypes = listOf(
        defaultsExtractorFactoryInterface.defaultType,
        irBuiltIns.anyType
    )
    defaultsExtractorFactoryClassImpl.createThisReceiverParameter()
    defaultsExtractorFactoryClassImpl.addConstructor { isPrimary = true }.apply {
        body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
            +irDelegatingConstructorCall(context.irBuiltIns.anyClass.owner.primaryConstructor!!)
        }
    }
    defaultsExtractorFactoryClassImpl.addOverridingMethod(
        context = pluginContext,
        function = defaultsExtractorFactoryInterface.getSimpleFunction("createDefaultsExtractor")!!.owner,
        block = { +irReturn(irCallConstructor(constructor)) }
    )
    currentFile.addChild(defaultsExtractorFactoryClassImpl)
    return defaultsExtractorFactoryClassImpl
}
private fun IrClass.addDefaultsExtractorConstructor(
    transformer: TransformerScope,
    classesToIntercept: List<IrClass>,
) {
    addConstructor {
        isPrimary = true
    }.apply {
        body = DeclarationIrBuilder(transformer.pluginContext, symbol).irBlockBody {
            +irDelegatingDefaultConstructorOrAny(transformer, classesToIntercept.firstOrNull { it.isClass })
        }
    }
}
