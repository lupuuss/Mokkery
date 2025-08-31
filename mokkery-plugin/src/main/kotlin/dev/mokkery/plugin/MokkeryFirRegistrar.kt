package dev.mokkery.plugin

import dev.mokkery.plugin.diagnostics.MokkeryDiagnostics
import dev.mokkery.plugin.diagnostics.MokkeryFirCheckersExtension
import dev.mokkery.plugin.fir.KtDiagnosticsContainerCompat
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class MokkeryFirRegistrar(private val config: CompilerConfiguration) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +{ session: FirSession -> MokkeryFirCheckersExtension(session, config) }
        registerDiagnosticContainersCompat(MokkeryDiagnostics)
    }

    private fun ExtensionRegistrarContext.registerDiagnosticContainersCompat(
        vararg diagnosticContainers: KtDiagnosticsContainerCompat
    ) {
        try {
            registerDiagnosticContainers(
                *diagnosticContainers
                    .map {it.container as KtDiagnosticsContainer }
                    .toTypedArray()
            )
        } catch (_: Exception) {
            val rootFactoryClass = Class.forName("org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory")
            val instance = rootFactoryClass.getField("INSTANCE").get(null)
            val registerFactory = rootFactoryClass.methods.first { it.name == "registerFactory" }
            diagnosticContainers.forEach {
                registerFactory.invoke(instance, it.getRendererFactory())
            }
        }
    }
}
