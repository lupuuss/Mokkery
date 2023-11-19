package dev.mokkery.plugin

import dev.mokkery.plugin.diagnostics.MokkeryFirCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class MokkeryFirRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::MokkeryFirCheckersExtension
    }
}
