package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.fir.KtDiagnosticsContainerCompat
import dev.mokkery.plugin.fir.isTemplatingFunction
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.matchingParameterFunctionType
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.types.typeAnnotations
import org.jetbrains.kotlin.name.isSubpackageOf
import org.jetbrains.kotlin.psi.KtElement

class TemplatingDeclarationChecker(
    private val configuration: CompilerConfiguration,
) : FirFunctionChecker(MppCheckerKind.Common) {

    private val templatingBlockAnnotation = Mokkery.ClassId.TemplatingLambda

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFunction) {
        if (!declaration.symbol.isTemplatingFunction()) return
        if (declaration.isAllowedTemplatingBlock(context.session)) return
        reporter.reportOn(declaration.source, Diagnostics.TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS)
    }

    private fun FirFunction.isAllowedTemplatingBlock(
        session: FirSession
    ): Boolean = isMokkeryTemplatingMethod() || this is FirAnonymousFunction && matchingParameterFunctionType
        ?.typeAnnotations
        ?.hasAnnotation(templatingBlockAnnotation, session) == true

    private fun FirFunction.isMokkeryTemplatingMethod()= this.symbol
        .packageFqName()
        .let {
            it.isSubpackageOf(Mokkery.dev_mokkery_templating) ||
                    it.isSubpackageOf(Mokkery.dev_mokkery_internal_templating)
        }

    object Diagnostics : KtDiagnosticsContainerCompat() {

        override fun getRendererFactory() = TemplatingDeclarationDiagnosticRendererFactory()

        val TEMPLATING_CANNOT_BE_EXTRACTED_TO_FUNCTIONS by error0<KtElement>()
    }
}
