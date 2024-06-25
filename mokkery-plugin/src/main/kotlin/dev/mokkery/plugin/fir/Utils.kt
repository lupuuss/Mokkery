package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType


fun FirConstructorSymbol.isDefault() = valueParameterSymbols.isEmpty() || valueParameterSymbols.all { it.hasDefaultValue }

val FirRegularClassSymbol.constructors get() = declarationSymbols.filterIsInstance<FirConstructorSymbol>()

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