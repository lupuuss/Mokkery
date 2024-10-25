package dev.mokkery.plugin.core

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

abstract class CoreTransformer(
    compilerPluginScope: CompilerPluginScope
) : TransformerScope, IrElementTransformerVoid() {
    final override lateinit var currentFile: IrFile
    override val classes = mutableMapOf<ClassResolver, IrClass>()
    override val functions = mutableMapOf<FunctionResolver, IrSimpleFunction>()
    override val properties = mutableMapOf<PropertyResolver, IrProperty>()
    override val compilerConfig: CompilerConfiguration = compilerPluginScope.compilerConfig
    override val pluginContext: IrPluginContext = compilerPluginScope.pluginContext

    override fun visitFile(declaration: IrFile): IrFile {
        currentFile = declaration
        return super.visitFile(declaration)
    }
}
