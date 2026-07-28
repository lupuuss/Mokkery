package dev.mokkery.mockable.plugin.fir.diagnostics

import dev.mokkery.mockable.plugin.fir.isMockableAnnotated
import dev.mokkery.plugin.core.fir.hasMokkeryGeneratedConstructor
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.isInner
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.getSuperClassSymbolOrAny
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtElement

class MokkeryMockableClassChecker : FirClassChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        if (declaration !is FirRegularClass) return
        if (!context.session.isMockableAnnotated(declaration)) return
        if (declaration.isLocal) reporter.reportOn(declaration.source, Diagnostics.LOCAL_CLASS_CANNOT_BE_MOCKABLE)
        if (declaration.isInner) reporter.reportOn(declaration.source, Diagnostics.INNER_CLASS_CANNOT_BE_MOCKABLE)
        val superClassSymbol = declaration.symbol.getSuperClassSymbolOrAny(context.session) ?: return
        if (superClassSymbol.classId == StandardClassIds.Any) return
        if (superClassSymbol.hasMokkeryGeneratedConstructor(context.session)) return
        reporter.reportOn(
            source = declaration.source,
            factory = Diagnostics.SUPER_CLASS_MUST_BE_MOCKABLE,
            a = declaration.symbol.defaultType(),
            b = superClassSymbol.defaultType()
        )
    }

    object Diagnostics : KtDiagnosticsContainer() {

        val SUPER_CLASS_MUST_BE_MOCKABLE by error2<KtElement, ConeKotlinType, ConeKotlinType>()
        val LOCAL_CLASS_CANNOT_BE_MOCKABLE by error0<KtElement>()
        val INNER_CLASS_CANNOT_BE_MOCKABLE by error0<KtElement>()

        override fun getRendererFactory() = MokkeryMockableClassDiagnosticRendererFactory()

    }
}
