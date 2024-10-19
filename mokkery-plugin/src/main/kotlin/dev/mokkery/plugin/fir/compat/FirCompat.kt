package dev.mokkery.plugin.fir.compat

import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
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

fun ConeKotlinType.toRegularClassSymbolCompat(session: FirSession): FirRegularClassSymbol? = try {
    toRegularClassSymbol(session)
} catch (e: NoClassDefFoundError) {
    toRegularClassSymbolMethod
        .invoke(null, this, session)
        ?.let { it as FirRegularClassSymbol }
}

private val toRegularClassSymbolMethod by lazy {
    ClassLoader.getSystemClassLoader()
        .loadClass("org.jetbrains.kotlin.fir.types.TypeUtilsKt")
        .methods
        .first { it.name == "toRegularClassSymbol" && it.parameters.size == 2 }
}
