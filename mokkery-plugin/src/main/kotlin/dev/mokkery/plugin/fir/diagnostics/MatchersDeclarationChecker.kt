package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.fir.acceptsMatcher
import dev.mokkery.plugin.fir.getMatcherAnnotation
import dev.mokkery.plugin.fir.hasMatcherScope
import dev.mokkery.plugin.fir.isInlinedInPlace
import dev.mokkery.plugin.fir.isMokkeryMatcherScope
import dev.mokkery.plugin.fir.signatureParameters
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.utils.isExternal
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.fir.types.lowerBoundIfFlexible
import org.jetbrains.kotlin.psi.KtElement

class MatchersDeclarationChecker(
    private val configuration: CompilerConfiguration,
) : FirFunctionChecker(MppCheckerKind.Common) {

    private val argMatcherClassId = Mokkery.ClassId.ArgMatcher

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFunction) {
        if (declaration.source?.kind is KtFakeSourceElementKind.DataClassGeneratedMembers) return
        checkMatcherScopeTypes(declaration)
        if (!declaration.symbol.hasMatcherScope()) return
        if (declaration.isInlinedInPlace()) return
        checkModality(declaration)
        checkExternal(declaration)
        checkOrigin(declaration)
        checkMatcherParams(declaration)
        if (declaration.source?.kind is KtFakeSourceElementKind) return
        val visitor = MatchersUsageReporterVisitor(
            session = context.session,
            context = context,
            reporter = reporter,
            configuration = configuration,
            parentFunction = declaration.symbol,
            usageContext = MatchersUsageContext.BUILDER
        )
        declaration.accept(visitor)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkMatcherScopeTypes(declaration: FirFunction) {
        declaration.ownParameters().forEach { (symbol, type) ->
            when {
                type.isMarkedNullable && type.isMokkeryMatcherScope() -> {
                    reporter.reportOn(symbol.source, Diagnostics.MATCHER_SCOPE_MUST_NOT_BE_NULLABLE)
                }
                type.isMatcherScopeTypeParameter() -> {
                    reporter.reportOn(symbol.source, Diagnostics.MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER)
                }
            }
        }
    }

    context(context: CheckerContext)
    private fun ConeKotlinType.isMatcherScopeTypeParameter(): Boolean {
        if (lowerBoundIfFlexible() !is ConeTypeParameterType) return false
        val scopeClass = context.session.getRegularClassSymbolByClassId(Mokkery.ClassId.MokkeryMatcherScope) ?: return false
        return isSubtypeOf(scopeClass.constructType(emptyArray(), isMarkedNullable = true), context.session)
    }

    private fun FirFunction.ownParameters(): List<Pair<FirBasedSymbol<*>, ConeKotlinType>> = when {
        this is FirPropertyAccessor && isSetter -> symbol.valueParameterSymbols.map { it to it.resolvedReturnType }
        else -> symbol.signatureParameters()
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkModality(declaration: FirFunction) {
        val modality = (declaration as? FirPropertyAccessor)?.propertySymbol?.modality ?: declaration.modality
        if (modality != Modality.FINAL) {
            reporter.reportOn(declaration.source, Diagnostics.MATCHER_MUST_BE_FINAL)
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkExternal(declaration: FirFunction) {
        if (declaration.isExternal || (declaration as? FirPropertyAccessor)?.propertySymbol?.isExternal == true) {
            reporter.reportOn(declaration.source, Diagnostics.MATCHER_MUST_NOT_BE_EXTERNAL)
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkOrigin(declaration: FirFunction) {
        val isLegal = when (declaration) {
            is FirNamedFunction -> !declaration.isSuspend
            is FirPropertyAccessor -> declaration.isGetter && declaration.propertySymbol.let { it.isVal && !it.hasDelegate }
            else -> false
        }
        if (!isLegal) reporter.reportOn(declaration.source, Diagnostics.MATCHER_MUST_BE_REGULAR_FUNCTION)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkMatcherParams(declaration: FirFunction) {
        val session = context.session
        declaration.ownParameters()
            .filter { (symbol, type) -> symbol.acceptsMatcher(session) && type.isArgMatcherOrArgMatcherScope(session) }
            .forEach { (symbol, type) ->
                reporter.reportOn(
                    source = symbol.getMatcherAnnotation(session)!!.source,
                    factory = Diagnostics.PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER,
                    a = type
                )
            }
    }

    private fun ConeKotlinType.isArgMatcherOrArgMatcherScope(
        session: FirSession
    ) = isArgMatcherType(session) || isMokkeryMatcherScope()

    private fun ConeKotlinType.isArgMatcherType(session: FirSession): Boolean {
        val argMatcherType = session.getRegularClassSymbolByClassId(argMatcherClassId)!!.defaultType()
        return classId == argMatcherClassId || isSubtypeOf(argMatcherType, session)
    }


    object Diagnostics : KtDiagnosticsContainer() {

        override fun getRendererFactory() = MatchersDeclarationDiagnosticRendererFactory()

        val MATCHER_MUST_BE_FINAL by error0<KtElement>()
        val MATCHER_MUST_NOT_BE_EXTERNAL by error0<KtElement>()
        val PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER by error1<KtElement, ConeKotlinType>()
        val MATCHER_MUST_BE_REGULAR_FUNCTION by error0<KtElement>()
        val MATCHER_SCOPE_MUST_NOT_BE_NULLABLE by error0<KtElement>()
        val MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER by error0<KtElement>()
    }
}
