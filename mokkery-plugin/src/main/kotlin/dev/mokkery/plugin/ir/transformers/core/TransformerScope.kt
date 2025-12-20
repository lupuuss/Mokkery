package dev.mokkery.plugin.ir.transformers.core

import dev.mokkery.plugin.ir.ClassResolver
import dev.mokkery.plugin.ir.FunctionResolver
import dev.mokkery.plugin.ir.PropertyResolver
import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

interface TransformerScope : CompilerPluginScope {

    val currentFileValue: IrFile
    val currentScopeValue: ScopeWithIr?

    val classes: MutableMap<ClassResolver, IrClass>
    val functions: MutableMap<FunctionResolver, IrSimpleFunction>
    val properties: MutableMap<PropertyResolver, IrProperty>
}

