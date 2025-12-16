package dev.mokkery.plugin

import dev.mokkery.plugin.core.enableFirDiagnostics
import dev.mokkery.plugin.diagnostics.MatchersDeclarationChecker
import dev.mokkery.plugin.diagnostics.MatchersUsageReporterVisitor
import dev.mokkery.plugin.diagnostics.MocksCreationChecker
import dev.mokkery.plugin.diagnostics.MokkeryFirCheckersExtension
import dev.mokkery.plugin.diagnostics.TemplatingChecker
import dev.mokkery.plugin.diagnostics.TemplatingDeclarationChecker
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class MokkeryFirRegistrar(private val config: CompilerConfiguration) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        if (config.enableFirDiagnostics) {
            +{ session: FirSession -> MokkeryFirCheckersExtension(session, config) }
            registerDiagnosticContainers(
                MatchersDeclarationChecker.Diagnostics,
                MatchersUsageReporterVisitor.Diagnostics,
                MocksCreationChecker.Diagnostics,
                TemplatingChecker.Diagnostics,
                TemplatingDeclarationChecker.Diagnostics,
            )
        }
    }
}
