package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.diagnostics.MatchersUsageReporterVisitor.Diagnostics
import dev.mokkery.plugin.fir.KtDiagnosticFactoryToRendererMapCompat
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

class MatchersUsageDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP by KtDiagnosticFactoryToRendererMapCompat("MokkeryMatchersUsageDiagnostic") {
        val scopeFunctionsHint = "If you''re trying to invoke a method with an extension receiver or context parameters, use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead."

        put(
            factory = Diagnostics.MATCHER_PASSED_TO_NON_MATCHER_PARAM,
            message = "''{0}'' does not accept matchers, but matcher argument is given. Mark parameter with @Matcher annotation, or use regular values.",
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION,
            message = "Matchers can only be passed to member functions, but were passed to ''{0}''. $scopeFunctionsHint",
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.ILLEGAL_METHOD_INVOCATION_ON_MATCHER,
            message = "Invoking methods on matchers is illegal."
        )
        put(
            factory = Diagnostics.ILLEGAL_OPERATOR_USAGE,
            message = "Operators cannot be used with matchers, but ''{0}'' operator is used.",
            rendererA = CommonRenderers.STRING
        )
        put(
            factory = Diagnostics.ILLEGAL_TRY_CATCH,
            message = "Returning matchers from try/catch is not supported."
        )
        put(
            factory = Diagnostics.ILLEGAL_NESTED_TEMPLATING,
            message = "''{0}'' calls cannot be nested in templating functions or matcher declarations.",
            rendererA = CommonRenderers.NAME
        )
        put(
            factory = Diagnostics.ILLEGAL_NESTED_FUNCTIONS_MATCHERS,
            message = "Matchers cannot be used in functions declared inside templating functions or matcher declarations, but used in ''{0}''. $scopeFunctionsHint",
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.ILLEGAL_NESTED_CLASS_MATCHERS,
            message = "Matchers cannot be used in classes declared inside templating functions or matcher declarations, but used in ''{0}''.",
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.ILLEGAL_MATCHER_IN_CONDITION,
            message = "Matcher cannot be used as a condition."
        )
        put(
            factory = Diagnostics.ILLEGAL_MATCHER_IN_WHEN_SUBJECT,
            message = "Matcher cannot be used as a when subject."
        )
        put(
            factory = Diagnostics.VARIABLE_OUT_OF_SCOPE,
            message = "''{0}'' is defined outside the templating scope and cannot be assigned using matchers.",
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.VARIABLE_NOT_A_MATCHER,
            message = "''{0}'' is not initialized with a matcher and therefore cannot be reassigned using one.",
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER,
            message = "Passing matchers to methods is not legal inside matcher builders."
        )
        put(
            factory = Diagnostics.MATCHER_PASSED_TO_NON_MEMBER_FUNCTION,
            message = "Matchers can be only passed to mock methods."
        )
        put(
            factory = Diagnostics.SINGLE_VARARG_MATCHER_ALLOWED,
            message = "Only one vararg matcher is allowed."
        )
        put(
            factory = Diagnostics.MATCHER_USED_WITH_FINAL_METHOD,
            message = "''{0}'' must not be used with matchers, because it''s final and cannot be mocked.",
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.MATCHES_WITH_COMPOSITE_ARG,
            message = "`dev.mokkery.matcher.matches` cannot be used with `ArgMatcher.Composite` matchers. To register composite matcher use `dev.mokkery.matcher.matchesComposite`."
        )
    }
}
