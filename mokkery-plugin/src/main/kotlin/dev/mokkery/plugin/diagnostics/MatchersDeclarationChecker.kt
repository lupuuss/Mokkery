package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.fir.KtDiagnosticsContainerCompat
import dev.mokkery.plugin.fir.acceptsMatcher
import dev.mokkery.plugin.fir.getMatcherAnnotation
import dev.mokkery.plugin.fir.isMatcher
import dev.mokkery.plugin.fir.isMokkeryMatcherScope
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.isExternal
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.psi.KtElement

class MatchersDeclarationChecker(
    private val configuration: CompilerConfiguration,
) : FirFunctionChecker(MppCheckerKind.Common) {

    private val argMatcherClassId = Mokkery.ClassId.ArgMatcher

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFunction) {
        if (!declaration.symbol.isMatcher()) return
        checkModality(declaration)
        checkExternal(declaration)
        checkOrigin(declaration)
        checkMatcherParams(declaration)
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
    private fun checkModality(declaration: FirFunction) {
        if (declaration.modality != Modality.FINAL) {
            reporter.reportOn(declaration.source, Diagnostics.MATCHER_MUST_BE_FINAL)
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkExternal(declaration: FirFunction) {
        if (declaration.isExternal) {
            reporter.reportOn(declaration.source, Diagnostics.MATCHER_MUST_NOT_BE_EXTERNAL)
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkOrigin(declaration: FirFunction) {
        if (declaration !is FirSimpleFunction) {
            reporter.reportOn(declaration.source, Diagnostics.MATCHER_MUST_BE_REGULAR_FUNCTION)
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkMatcherParams(declaration: FirFunction) {
        (declaration.contextParameters + declaration.valueParameters).forEach {
            val symbol = it.symbol
            val symbolType = it.symbol.resolvedReturnType
            val session = context.session
            if (symbol.acceptsMatcher(session) && symbolType.isArgMatcherOrArgMatcherScope(session)) {
                reporter.reportOn(
                    source = symbol.getMatcherAnnotation(session)!!.source,
                    factory = Diagnostics.PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER,
                    a = symbolType
                )
            }
        }
        val receiverSymbol = declaration.receiverParameter?.symbol ?: return
        val receiverSymbolType = receiverSymbol.resolvedType
        val session = context.session
        if (receiverSymbol.acceptsMatcher(session) && receiverSymbolType.isArgMatcherOrArgMatcherScope(session)) {
            reporter.reportOn(
                source = receiverSymbol.getMatcherAnnotation(session)!!.source,
                factory = Diagnostics.PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER,
                a = receiverSymbolType
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


    object Diagnostics : KtDiagnosticsContainerCompat() {

        override fun getRendererFactory() = MatchersDeclarationDiagnosticRendererFactory()

        val MATCHER_MUST_BE_FINAL by error0<KtElement>()
        val MATCHER_MUST_NOT_BE_EXTERNAL by error0<KtElement>()
        val PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER by error1<KtElement, ConeKotlinType>()
        val MATCHER_MUST_BE_REGULAR_FUNCTION by error0<KtElement>()
    }
}
