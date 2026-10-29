package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.resolve.fullyExpandedType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.psi.KtElement

class ModuleInitializerDeclarationChecker : FirFunctionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFunction) {
        if (!declaration.hasAnnotation(Mokkery.ClassId.MokkeryModuleInit, context.session)) return
        when {
            !declaration.isTopLevelNamedFunction() -> reporter.reportOn(declaration.source, Diagnostics.MODULE_INITIALIZER_MUST_BE_TOP_LEVEL_FUNCTION)
            declaration.visibility == Visibilities.Private -> reporter.reportOn(declaration.source, Diagnostics.MODULE_INITIALIZER_MUST_NOT_BE_PRIVATE)
            declaration.isSuspend -> reporter.reportOn(declaration.source, Diagnostics.MODULE_INITIALIZER_MUST_NOT_BE_SUSPEND)
            declaration.typeParameters.isNotEmpty() -> reporter.reportOn(declaration.source, Diagnostics.MODULE_INITIALIZER_MUST_NOT_HAVE_TYPE_PARAMETERS)
            !declaration.hasAcceptedParameters() -> reporter.reportOn(declaration.source, Diagnostics.MODULE_INITIALIZER_INVALID_PARAMETERS)
        }
    }

    context(context: CheckerContext)
    private fun FirFunction.hasAcceptedParameters(): Boolean {
        val types = contextParameters.map { it.returnTypeRef.coneType } +
                listOfNotNull(receiverParameter?.typeRef?.coneType) +
                valueParameters.map { if (it.isVararg) null else it.returnTypeRef.coneType }
        return types.isEmpty() || types.singleOrNull()?.isAccepted() == true
    }

    context(context: CheckerContext)
    private fun ConeKotlinType?.isAccepted(): Boolean {
        val type = this?.fullyExpandedType() ?: return false
        return !type.isMarkedNullable && (type.classId == Mokkery.ClassId.MokkeryConfigurer || type.classId == Mokkery.ClassId.MokkeryScope)
    }

    object Diagnostics : KtDiagnosticsContainer() {

        override fun getRendererFactory() = ModuleInitializerDeclarationDiagnosticRendererFactory()

        val MODULE_INITIALIZER_MUST_BE_TOP_LEVEL_FUNCTION by error0<KtElement>()
        val MODULE_INITIALIZER_MUST_NOT_BE_PRIVATE by error0<KtElement>()
        val MODULE_INITIALIZER_MUST_NOT_BE_SUSPEND by error0<KtElement>()
        val MODULE_INITIALIZER_MUST_NOT_HAVE_TYPE_PARAMETERS by error0<KtElement>()
        val MODULE_INITIALIZER_INVALID_PARAMETERS by error0<KtElement>()
    }
}

private fun FirFunction.isTopLevelNamedFunction(): Boolean {
    return this is FirNamedFunction && !this.isLocal && this.dispatchReceiverType == null
}
