package dev.mokkery.tests

import dev.mokkery.mockable.internal.options.MokkeryMockableOptions

object MokkeryMockableDirectives : OptionDirectivesContainer() {

    val DISABLE_FIR_DIAGNOSTICS by flagOptionDirective(
        option = MokkeryMockableOptions.enableFirDiagnostics,
        description = "Disables Mokkery Mockable's FIR diagnostics",
        value = false
    )
}
