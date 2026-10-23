package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.fir.diagnostics.ModuleInitializerDeclarationChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory

class ModuleInitializerDeclarationDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP by KtDiagnosticFactoryToRendererMap("MokkeryModuleInitializerDeclarationDiagnostic") {
        it.put(
            factory = Diagnostics.MODULE_INITIALIZER_MUST_BE_TOP_LEVEL_FUNCTION,
            message = "Module initializer must be a top-level function."
        )
        it.put(
            factory = Diagnostics.MODULE_INITIALIZER_MUST_NOT_BE_PRIVATE,
            message = "Module initializer must not be private."
        )
        it.put(
            factory = Diagnostics.MODULE_INITIALIZER_MUST_NOT_BE_SUSPEND,
            message = "Module initializer must not be suspend."
        )
        it.put(
            factory = Diagnostics.MODULE_INITIALIZER_MUST_NOT_HAVE_TYPE_PARAMETERS,
            message = "Module initializer must not have type parameters."
        )
        it.put(
            factory = Diagnostics.MODULE_INITIALIZER_INVALID_PARAMETERS,
            message = "Module initializer must have no parameters or a single parameter of type 'dev.mokkery.configurer.MokkeryConfigurer' or 'dev.mokkery.MokkeryScope'."
        )
    }
}
