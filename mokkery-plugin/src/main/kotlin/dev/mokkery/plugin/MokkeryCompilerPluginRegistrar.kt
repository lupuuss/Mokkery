package dev.mokkery.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.devkit.DevKitComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class MokkeryCompilerPluginRegistrar : DevKitComponentRegistrar {

    override fun CompilerPluginRegistrar.ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(MokkeryIrGenerationExtension(configuration))
        FirExtensionRegistrarAdapter.registerExtension(MokkeryFirRegistrar(configuration))
    }
}
