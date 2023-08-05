package dev.mokkery.plugin.core

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration

interface CompilerPluginScope {

    val compilerConfig: CompilerConfiguration

    val pluginContext: IrPluginContext
}

fun CompilerPluginScope(config: CompilerConfiguration, context: IrPluginContext): CompilerPluginScope {
    return CompilerPluginScopeImpl(config, context)
}

private class CompilerPluginScopeImpl(
    override val compilerConfig: CompilerConfiguration,
    override val pluginContext: IrPluginContext
) : CompilerPluginScope
