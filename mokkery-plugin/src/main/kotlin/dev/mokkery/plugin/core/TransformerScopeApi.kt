package dev.mokkery.plugin.core

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.name.ClassId
import kotlin.reflect.KClass

val TransformerScope.messageCollector get() = compilerConfig
    .get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

fun TransformerScope.getClass(resolver: ClassResolver): IrClass = classes.getOrPut(resolver) {
    resolver.resolve(pluginContext)
}

fun TransformerScope.getFunction(resolver: FunctionResolver): IrSimpleFunction = functions.getOrPut(resolver) {
    resolver.resolve(pluginContext)
}

fun TransformerScope.getIrClassOf(cls: KClass<*>) = pluginContext
    .referenceClass(ClassId.fromString(cls.qualifiedName!!))!!
    .owner
