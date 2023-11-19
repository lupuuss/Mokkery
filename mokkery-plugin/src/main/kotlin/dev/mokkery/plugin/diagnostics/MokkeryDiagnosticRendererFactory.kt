package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.Errors
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

class MokkeryDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP = KtDiagnosticFactoryToRendererMap("MokkeryDiagnostic").apply {
        put(
            factory = MokkeryDiagnostics.INDIRECT_INTERCEPTION,
            message = Errors.indirectCall(typeArgument = "{1}", functionName = "{0}"),
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.RENDER_TYPE
        )
        put(
            factory = MokkeryDiagnostics.FUNCTIONAL_PARAM_MUST_BE_LAMBDA,
            message = Errors.notLambdaExpression(functionName = "{0}", param = "{1}"),
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.SYMBOL,
        )
        put(
            factory = MokkeryDiagnostics.SEALED_TYPE_CANNOT_BE_INTERCEPTED,
            message = Errors.sealedTypeCannotBeIntercepted(typeName = "{1}", functionName = "{0}"),
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.RENDER_TYPE,
        )
        put(
            factory = MokkeryDiagnostics.FINAL_TYPE_CANNOT_BE_INTERCEPTED,
            message = Errors.finalTypeCannotBeIntercepted(typeName = "{1}", functionName = "{0}"),
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.RENDER_TYPE,
        )
        put(
            factory = MokkeryDiagnostics.FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED,
            message = Errors.finalMembersTypeCannotBeIntercepted(
                typeName = "{1}",
                functionName = "{0}",
                nonAbstractMembers = "{2}"
            ),
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.RENDER_TYPE,
            rendererC = CommonRenderers.commaSeparated(FirDiagnosticRenderers.SYMBOL)
        )

        put(
            factory = MokkeryDiagnostics.NO_DEFAULT_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED,
            message = Errors.noDefaultConstructorTypeCannotBeIntercepted(
                typeName = "{1}",
                functionName = "{0}",
            ),
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.RENDER_TYPE,
        )
    }
}
