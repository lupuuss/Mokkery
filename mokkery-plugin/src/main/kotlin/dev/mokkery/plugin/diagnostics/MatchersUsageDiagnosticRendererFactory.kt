package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.Mokkery.MatcherUsageErrors
import dev.mokkery.plugin.diagnostics.MatchersUsageReporterVisitor.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

class MatchersUsageDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP = KtDiagnosticFactoryToRendererMap("MokkeryMatchersUsageDiagnostic").apply {
        put(
            factory = Diagnostics.MATCHER_PASSED_TO_NON_MATCHER_PARAM,
            message = MatcherUsageErrors.matcherPassedToNonMatcherParam("{0}"),
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION,
            message = MatcherUsageErrors.illegalMatcherInNonMemberFunction("{0}"),
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.ILLEGAL_METHOD_INVOCATION_ON_MATCHER,
            message = MatcherUsageErrors.illegalMethodInvocationOnMatcher(),
        )
        put(
            factory = Diagnostics.ILLEGAL_VARARGS_COMPOSITE,
            message = MatcherUsageErrors.illegalVarargsComposite()
        )
        put(
            factory = Diagnostics.ILLEGAL_OPERATOR_USAGE,
            message = MatcherUsageErrors.illegalOperatorUsageOnMatcher("{0}"),
            rendererA = CommonRenderers.STRING
        )
        put(
            factory = Diagnostics.ILLEGAL_TRY_CATCH,
            message = MatcherUsageErrors.illegalTryCatch()
        )
        put(
            factory = Diagnostics.VARARG_REQUIRED_IN_ALL_BRANCHES,
            message = MatcherUsageErrors.varargRequiredInAllBranches()
        )
        put(
            factory = Diagnostics.ILLEGAL_NESTED_TEMPLATING,
            message = MatcherUsageErrors.illegalNestedTemplating(function = "{0}"),
            rendererA = CommonRenderers.NAME,
        )
        put(
            factory = Diagnostics.ILLEGAL_NESTED_FUNCTIONS_MATCHERS,
            message = MatcherUsageErrors.illegalNestedFunctionsMatchers(function = "{0}"),
            rendererA = FirDiagnosticRenderers.SYMBOL,
        )
        put(
            factory = Diagnostics.ILLEGAL_NESTED_CLASS_MATCHERS,
            message = MatcherUsageErrors.illegalNestedClassMatchers(klass = "{0}"),
            rendererA = FirDiagnosticRenderers.SYMBOL,
        )
        put(
            factory = Diagnostics.ILLEGAL_MATCHER_IN_CONDITION,
            message = MatcherUsageErrors.illegalMatcherInCondition()
        )
        put(
            factory = Diagnostics.VARIABLE_OUT_OF_SCOPE,
            message = MatcherUsageErrors.variableDefinedOutOfScope("{0}"),
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.VARIABLE_NOT_A_MATCHER,
            message = MatcherUsageErrors.variableNotMatcher("{0}"),
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.INCOMPATIBLE_VARIABLE_TYPE,
            message = MatcherUsageErrors.incompatibleVariableMatcherType(
                variable = "{0}",
                variableMatcherType = "{1}",
                assignedMatcherType = "{2}"
            ),
            rendererA = FirDiagnosticRenderers.SYMBOL,
            rendererB = CommonRenderers.STRING,
            rendererC = CommonRenderers.STRING
        )
        put(
            factory = Diagnostics.MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER,
            message = MatcherUsageErrors.matcherPassedToMethodInMatcherBuilder()
        )
        put(
            factory = Diagnostics.ILLEGAL_SPREAD_FOR_VARARG,
            message = MatcherUsageErrors.illegalSpreadForVararg()
        )
        put(
            factory = Diagnostics.SINGLE_VARARG_MATCHER_ALLOWED,
            message = MatcherUsageErrors.singleVarargMatcherAllowed()
        )
        put(
            factory = Diagnostics.VARARG_MATCHER_USED_WITHOUT_SPREAD,
            message = MatcherUsageErrors.varargMatcherUsedWithoutSpread()
        )
        put(
            factory = Diagnostics.VARARG_MATCHER_WITHOUT_VARARG,
            message = MatcherUsageErrors.varargMatcherUsedWithoutVararg()
        )
        put(
            factory = Diagnostics.MATCHER_PASSED_TO_NON_MEMBER_FUNCTION,
            message = MatcherUsageErrors.matcherPassedToNonMemberFunction()
        )
        put(
            factory = Diagnostics.VARARG_MATCHER_BUILDER_MUST_RETURN_VARARG_MATCHERS_ONLY,
            message = MatcherUsageErrors.varargMatcherBuilderMustReturnVarargMatchersOnly(),
        )
        put(
            factory = Diagnostics.MATCHER_RETURNING_VARARG_MATCHER_MUST_BE_ANNOTATED,
            message = MatcherUsageErrors.matcherReturningVarargMatcherMustBeAnnotated(),
        )
        put(
            factory = Diagnostics.MATCHER_USED_WITH_FINAL_METHOD,
            message = MatcherUsageErrors.matcherUsedWithFinalMethod("{0}"),
            rendererA = FirDiagnosticRenderers.SYMBOL
        )
        put(
            factory = Diagnostics.MATCHES_WITH_COMPOSITE_ARG,
            message = MatcherUsageErrors.matchesWithCompositeArg(),
        )
    }
}
