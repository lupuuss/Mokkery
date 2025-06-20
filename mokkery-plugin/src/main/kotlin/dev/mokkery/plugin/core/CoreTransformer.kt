package dev.mokkery.plugin.core

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

abstract class CoreTransformer(
    compilerPluginScope: CompilerPluginScope
) : TransformerScope, IrElementTransformerVoidWithContext() {

    override val currentFileValue: IrFile
        get() = currentFile

    override val currentScopeValue: ScopeWithIr
        get() = currentScope!!

    override val classes = mutableMapOf<ClassResolver, IrClass>()
    override val functions = mutableMapOf<FunctionResolver, IrSimpleFunction>()
    override val properties = mutableMapOf<PropertyResolver, IrProperty>()
    override val compilerConfig: CompilerConfiguration = compilerPluginScope.compilerConfig
    override val pluginContext: IrPluginContext = compilerPluginScope.pluginContext

    fun <T> withScope(scope: ScopeWithIr, block: () -> T): T {
        unsafeEnterScope(scope.scope.scopeOwnerSymbol.owner)
        return block().also { unsafeLeaveScope() }
    }
}
