package dev.mokkery.mockable.plugin

import com.google.auto.service.AutoService
import dev.mokkery.mockable.internal.MokkeryMockableConfig
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@AutoService(CompilerPluginRegistrar::class)
class MokkeryMockableCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val pluginId = MokkeryMockableConfig.PLUGIN_ID

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(MokkeryMockableIrGenerationExtension(configuration))
        FirExtensionRegistrarAdapter.registerExtension(MokkeryMockableFirRegistrar(configuration))
    }
}
