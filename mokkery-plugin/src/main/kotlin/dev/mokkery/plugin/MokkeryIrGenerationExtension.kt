package dev.mokkery.plugin

import dev.mokkery.plugin.ir.transformers.core.CompilerPluginScope
import dev.mokkery.plugin.ir.transformers.MokkeryTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class MokkeryIrGenerationExtension(
    private val config: CompilerConfiguration,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        MokkeryTransformer(CompilerPluginScope(config, pluginContext))
            .visitModuleFragment(moduleFragment)
    }
}
