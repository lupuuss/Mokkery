package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery.Callable
import org.jetbrains.kotlin.AbstractKtSourceElement
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.error3
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.declarations.utils.isSuspend
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtElement

class TemplatingChecker(
    private val configuration: CompilerConfiguration,
) : FirFunctionCallChecker(MppCheckerKind.Common) {

    private val templatingFunctions = setOf(
        Callable.every,
        Callable.everySuspend,
        Callable.verify,
        Callable.verifySuspend
    )

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callee = expression.calleeReference as? FirResolvedNamedReference ?: return
        val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return
        context(symbol) {
            when (symbol.callableId) {
                in templatingFunctions -> checkTemplating(expression)
            }
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkTemplating(expression: FirFunctionCall) {
        val blockArgument = expression
            .arguments
            .last()
            .unwrapArgument()
        checkTemplatingArgument(blockArgument.source ?: expression.source, blockArgument)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkTemplatingArgument(source: AbstractKtSourceElement?, argument: FirExpression) {
        val blockParameter = funSymbol
            .valueParameterSymbols
            .firstOrNull()
        val classId = blockParameter?.resolvedReturnType?.classId
        when (classId) {
            StandardClassIds.KFunction -> checkTemplatingReference(source, argument)
            else -> checkTemplatingBlock(source, argument)
        }
    }


    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkTemplatingReference(source: AbstractKtSourceElement?, argument: FirExpression) {
        if (argument !is FirCallableReferenceAccess) return reporter.reportOn(
            source = argument.source,
            factory = Diagnostics.FUNCTIONAL_PARAM_MUST_BE_REFERENCE,
            a = funSymbol.name,
            b = funSymbol.valueParameterSymbols.last(),
        )
        val referenceFunction = argument.calleeReference.toResolvedFunctionSymbol() ?: return
        val isTemplatingForSuspend = funSymbol.name.asString().endsWith("Suspend")
        if (referenceFunction.isSuspend != isTemplatingForSuspend) {
            return reporter.reportOn(
                source = source,
                factory = Diagnostics.FUNCTION_REFERENCE_INCORRECT_TYPE,
                a = funSymbol.name,
                b = if (referenceFunction.isSuspend) "suspend function" else "regular function",
                c = if (isTemplatingForSuspend) "suspend function" else "regular function",
            )
        }
        if (argument.dispatchReceiver == null) {
            return reporter.reportOn(
                source = source,
                factory = Diagnostics.FUNCTION_REFERENCE_NOT_BOUND,
                a = funSymbol.name,
            )
        }
        val receiver = argument.dispatchReceiver
        if (receiver !is FirCallableReferenceAccess) return
        val symbol = receiver.toResolvedCallableSymbol() as? FirPropertySymbol
        val functionName = referenceFunction.name.asString()
        when {
            symbol != null && functionName in setOf("get", "set") -> {
                if (receiver.dispatchReceiver !is FirCallableReferenceAccess) return
            }
            symbol != null -> return reporter.reportOn(
                source = source,
                factory = Diagnostics.PROPERTY_FUNCTION_REFERENCE_MUST_BE_ACCESSOR,
                a = referenceFunction.name
            )
        }
        return reporter.reportOn(
            source = source,
            factory = Diagnostics.FUNCTION_REFERENCE_CHAIN_NOT_ALLOWED,
        )
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkTemplatingBlock(source: AbstractKtSourceElement?, blockArgument: FirExpression) {
        if (blockArgument !is FirAnonymousFunctionExpression) {
            return reporter.reportOn(
                source = source,
                factory = Diagnostics.FUNCTIONAL_PARAM_MUST_BE_LAMBDA,
                a = funSymbol.name,
                b = funSymbol.valueParameterSymbols.last(),
            )
        }
        val visitor = MatchersUsageReporterVisitor(
            session = context.session,
            context = context,
            reporter = reporter,
            configuration = configuration,
            parentFunction = blockArgument.anonymousFunction.symbol,
            usageContext = MatchersUsageContext.TEMPLATING
        )
        blockArgument.accept(visitor)
    }

    object Diagnostics : KtDiagnosticsContainer() {

        override fun getRendererFactory() = TemplatingDiagnosticRendererFactory()

        val FUNCTIONAL_PARAM_MUST_BE_LAMBDA by error2<KtElement, Name, FirValueParameterSymbol>()
        val FUNCTIONAL_PARAM_MUST_BE_REFERENCE by error2<KtElement, Name, FirValueParameterSymbol>()
        val FUNCTION_REFERENCE_INCORRECT_TYPE by error3<KtElement, Name, String, String>()
        val FUNCTION_REFERENCE_NOT_BOUND by error1<KtElement, Name>()
        val FUNCTION_REFERENCE_CHAIN_NOT_ALLOWED by error0<KtElement>()
        val PROPERTY_FUNCTION_REFERENCE_MUST_BE_ACCESSOR by error1<KtElement, Name>()
    }
}
