package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.fir.diagnostics.TemplatingDeclarationChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory


class TemplatingDeclarationDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP by KtDiagnosticFactoryToRendererMap("MokkeryTemplatingDeclarationDiagnostic") {
        it.put(
            factory = Diagnostics.TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS,
            message = "Extracting templating to separate functions is currently illegal. Templating must be performed directly inside lambda passed to `every`/`verify`.",
        )
    }
}
