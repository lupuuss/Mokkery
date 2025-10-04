package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.fir.KtDiagnosticFactoryToRendererMapCompat
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.types.ConeKotlinType

class MokkeryDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    private val typeRenderer: DiagnosticParameterRenderer<ConeKotlinType> = FirDiagnosticRenderers.RENDER_TYPE

    override val MAP by KtDiagnosticFactoryToRendererMapCompat("MokkeryDiagnostic") {
        put(
            factory = MokkeryDiagnostics.INDIRECT_INTERCEPTION,
            message = "''{1}'' is a type parameter. Specific type expected for a ''{0}'' call.",
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer
        )
        put(
            factory = MokkeryDiagnostics.FUNCTIONAL_PARAM_MUST_BE_LAMBDA,
            message =  "Argument passed to ''{0}'' for param ''{1}'' must be a lambda expression!",
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.SYMBOL,
        )
        put(
            factory = MokkeryDiagnostics.SEALED_TYPE_CANNOT_BE_INTERCEPTED,
            message = "Type ''{1}'' is sealed and cannot be used with ''{0}''.",
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
        )
        put(
            factory = MokkeryDiagnostics.FINAL_TYPE_CANNOT_BE_INTERCEPTED,
            message = "Type ''{1}'' is final and cannot be used with ''{0}''.",
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
        )
        put(
            factory = MokkeryDiagnostics.PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED,
            message = "Type ''{1}'' is primitive and cannot be used with ''{0}''.",
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
        )
        put(
            factory = MokkeryDiagnostics.FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED,
            message = "Type ''{1}'' has final members and cannot be used with ''{0}''. Final members: {2}",
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
            rendererC = CommonRenderers.commaSeparated(FirDiagnosticRenderers.SYMBOL)
        )
        put(
            factory = MokkeryDiagnostics.NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED,
            message = "Type ''{1}'' has no public constructor and cannot be used with ''{0}''.",
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
        )
        put(
            factory = MokkeryDiagnostics.MULTIPLE_SUPER_CLASSES_FOR_MOCK_MANY,
            message = "Only one super class is acceptable for ''{0}'' type. Detected super classes: {1}",
            rendererA = CommonRenderers.NAME,
            rendererB = CommonRenderers.commaSeparated(typeRenderer)
        )
        put(
            factory = MokkeryDiagnostics.DUPLICATE_TYPES_FOR_MOCK_MANY,
            message = "Type ''{0}'' for ''{1}'' must occur only once, but it occurs {2} times.",
            rendererA = typeRenderer,
            rendererB = CommonRenderers.NAME,
            rendererC = CommonRenderers.STRING
        )
        put(
            factory = MokkeryDiagnostics.FUNCTIONAL_TYPE_ON_JS_FOR_MOCK_MANY,
            message = "Type ''{0}'' is a functional type and it is not acceptable as an argument fo`r ''{1}'' on JS platform.",
            rendererA = typeRenderer,
            rendererB = CommonRenderers.NAME
        )
    }
}
