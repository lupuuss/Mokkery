package dev.mokkery.mockable.plugin

import dev.mokkery.mockable.plugin.fir.FirMockableAnnotationPredicateMatcher
import dev.mokkery.mockable.plugin.fir.diagnostics.MokkeryMockableFirCheckersExtension
import dev.mokkery.mockable.plugin.fir.gen.MokkeryMockableGenerator
import dev.mokkery.mockable.plugin.fir.status.MokkeryMockableFirStatusTransformerExtension
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class MokkeryMockableFirRegistrar(private val config: CompilerConfiguration) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +{ session: FirSession -> FirMockableAnnotationPredicateMatcher(session, config.annotationFqNames.toList()) }
        +::MokkeryMockableGenerator
        +::MokkeryMockableFirStatusTransformerExtension
        if (config.enableFirDiagnostics) {
            +::MokkeryMockableFirCheckersExtension
        }
    }
}
