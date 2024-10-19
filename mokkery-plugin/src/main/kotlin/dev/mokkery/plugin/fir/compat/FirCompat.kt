package dev.mokkery.plugin.fir.compat

import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.types.ConeKotlinType

@Suppress("UNCHECKED_CAST")
fun FirDiagnosticRenderers.renderTypeCompat(): DiagnosticParameterRenderer<ConeKotlinType> = try {
    RENDER_TYPE
} catch (e: NoSuchMethodError) {
    FirDiagnosticRenderers::class
        .java
        .methods
        .first { it.name == "getRENDER_TYPE" }
        .invoke(this)
        .let { it as DiagnosticParameterRenderer<ConeKotlinType> }
}
