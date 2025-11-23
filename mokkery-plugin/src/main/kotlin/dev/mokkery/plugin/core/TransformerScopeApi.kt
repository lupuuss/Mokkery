package dev.mokkery.plugin.core

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.companionObject

val TransformerScope.messageCollector get() = compilerConfig.messageCollector

fun TransformerScope.getClass(resolver: ClassResolver): IrClass = classes.getOrPut(resolver) {
    resolver.resolve(pluginContext)
}

fun TransformerScope.getCompanionOf(resolver: ClassResolver): IrClass = getClass(resolver).companionObject()!!

fun TransformerScope.getFunction(resolver: FunctionResolver): IrSimpleFunction = functions.getOrPut(resolver) {
    resolver.resolve(pluginContext)
}

fun TransformerScope.getProperty(resolver: PropertyResolver): IrProperty = properties.getOrPut(resolver) {
    resolver.resolve(pluginContext)
}

inline val TransformerScope.platform get() = pluginContext.platform

inline fun <T> TransformerScope.declarationIrBuilder(
    context: IrGeneratorContext = pluginContext,
    block: DeclarationIrBuilder.() -> T
) = DeclarationIrBuilder(context, currentScopeValue!!.scope.scopeOwnerSymbol).run(block)
