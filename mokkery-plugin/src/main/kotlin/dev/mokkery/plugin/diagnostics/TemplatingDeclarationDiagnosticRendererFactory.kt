package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.TemplatingDeclarationErrors
import dev.mokkery.plugin.diagnostics.TemplatingDeclarationChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory


class TemplatingDeclarationDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP = KtDiagnosticFactoryToRendererMap("MokkeryTemplatingDeclarationDiagnostic").apply {
        put(
            factory = Diagnostics.TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS,
            message = TemplatingDeclarationErrors.extractingTemplatingFunctionsIsCurrentlyIllegal(),
        )
    }
}
