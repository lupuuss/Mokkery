package dev.mokkery.plugin

import com.google.auto.service.AutoService
import dev.mokkery.plugin.diagnostics.MokkeryDiagnosticRendererFactory
import dev.mokkery.plugin.jvm.MokkeryClassGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.extensions.ClassGeneratorExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@AutoService(CompilerPluginRegistrar::class)
class MokkeryCompilerPluginRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(MokkeryIrGenerationExtension(configuration))
        FirExtensionRegistrarAdapter.registerExtension(MokkeryFirRegistrar())
        RootDiagnosticRendererFactory.registerFactory(MokkeryDiagnosticRendererFactory())
        ClassGeneratorExtension.registerExtension(MokkeryClassGenerationExtension(configuration))
    }
}
