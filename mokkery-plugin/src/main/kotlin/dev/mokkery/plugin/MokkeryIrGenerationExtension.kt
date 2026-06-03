package dev.mokkery.plugin

import dev.mokkery.plugin.core.CacheStore
import dev.mokkery.plugin.core.context.asMokkeryContext
import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.asMokkeryContext
import dev.mokkery.plugin.ir.transformer.MokkeryRootTransformer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class MokkeryIrGenerationExtension(
    private val configuration: CompilerConfiguration,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val context = configuration.asMokkeryContext() + pluginContext.asMokkeryContext() + CacheStore()
        MokkeryRootTransformer(IrMokkeryPluginScope(context))
            .visitModuleFragment(moduleFragment)
    }
}
