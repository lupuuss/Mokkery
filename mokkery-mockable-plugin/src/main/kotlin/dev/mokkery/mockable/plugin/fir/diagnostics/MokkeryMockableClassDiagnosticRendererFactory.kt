package dev.mokkery.mockable.plugin.fir.diagnostics

import dev.mokkery.mockable.plugin.fir.diagnostics.MokkeryMockableClassChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

class MokkeryMockableClassDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP by KtDiagnosticFactoryToRendererMap("MokkeryMockableDiagnostic") {
        it.put(
            factory = Diagnostics.SUPER_CLASS_MUST_BE_MOCKABLE,
            message = "Class ''{0}'' cannot be marked as mockable, because it's super class ''{1}'' is not marked as mockable.",
            rendererA = FirDiagnosticRenderers.RENDER_TYPE,
            rendererB = FirDiagnosticRenderers.RENDER_TYPE,
        )
        it.put(
            factory = Diagnostics.LOCAL_CLASS_CANNOT_BE_MOCKABLE,
            message = "Local class cannot be made mockable."
        )
        it.put(
            factory = Diagnostics.INNER_CLASS_CANNOT_BE_MOCKABLE,
            message = "Inner class cannot be made mockable."
        )
    }
}
