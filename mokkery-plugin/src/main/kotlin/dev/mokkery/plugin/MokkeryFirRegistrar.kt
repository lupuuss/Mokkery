package dev.mokkery.plugin

import dev.mokkery.plugin.ir.transformers.core.enableFirDiagnostics
import dev.mokkery.plugin.fir.diagnostics.MatchersDeclarationChecker
import dev.mokkery.plugin.fir.diagnostics.MatchersUsageReporterVisitor
import dev.mokkery.plugin.fir.diagnostics.MocksCreationChecker
import dev.mokkery.plugin.fir.diagnostics.MokkeryFirCheckersExtension
import dev.mokkery.plugin.fir.diagnostics.TemplatingChecker
import dev.mokkery.plugin.fir.diagnostics.TemplatingDeclarationChecker
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
