package dev.mokkery.plugin

import dev.mokkery.plugin.core.enableFirDiagnostics
import dev.mokkery.plugin.diagnostics.MatchersDeclarationChecker
import dev.mokkery.plugin.diagnostics.MatchersUsageReporterVisitor
import dev.mokkery.plugin.diagnostics.MocksCreationChecker
import dev.mokkery.plugin.diagnostics.MokkeryFirCheckersExtension
import dev.mokkery.plugin.diagnostics.TemplatingChecker
import dev.mokkery.plugin.diagnostics.TemplatingDeclarationChecker
import dev.mokkery.plugin.fir.KtDiagnosticsContainerCompat
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class MokkeryFirRegistrar(private val config: CompilerConfiguration) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        if (config.enableFirDiagnostics) {
            +{ session: FirSession -> MokkeryFirCheckersExtension(session, config) }
            registerDiagnosticContainersCompat(
                MatchersDeclarationChecker.Diagnostics,
                MatchersUsageReporterVisitor.Diagnostics,
                MocksCreationChecker.Diagnostics,
                TemplatingChecker.Diagnostics,
                TemplatingDeclarationChecker.Diagnostics,
            )
        }
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
