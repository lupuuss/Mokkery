package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.TemplatingDeclarationErrors
import dev.mokkery.plugin.diagnostics.TemplatingDeclarationChecker.Diagnostics
import dev.mokkery.plugin.fir.KtDiagnosticFactoryToRendererMapCompat
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory


class TemplatingDeclarationDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP by KtDiagnosticFactoryToRendererMapCompat("MokkeryTemplatingDeclarationDiagnostic") {
        put(
            factory = Diagnostics.TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS,
            message = TemplatingDeclarationErrors.extractingTemplatingFunctionsIsCurrentlyIllegal(),
        )
    }
}
