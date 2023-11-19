package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.Callable
import dev.mokkery.plugin.fir.constructors
import dev.mokkery.plugin.fir.isDefault
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.toConeTypeProjection
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.type

class MokkeryCallsChecker(private val session: FirSession) : FirFunctionCallChecker() {
    private val mock = Callable.mock
    val spy = Callable.spy
    val every = Callable.every
    val everySuspend = Callable.everySuspend
    val verify = Callable.verify
    val verifySuspend = Callable.verifySuspend

    override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {
        val callee = expression.calleeReference as? FirResolvedNamedReference ?: return
        val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return
        val scope = MokkeryScopedCallsChecker(
            session = session,
            expression = expression,
            context = context,
            reporter = reporter,
            funSymbol = symbol,
        )
        when (symbol.callableId) {
            mock, spy -> scope.checkInterception()
            every, everySuspend, verify, verifySuspend -> scope.checkTemplating()
        }
    }
}

private class MokkeryScopedCallsChecker(
    private val session: FirSession,
    private val expression: FirFunctionCall,
    private val context: CheckerContext,
    private val reporter: DiagnosticReporter,
    private val funSymbol: FirNamedFunctionSymbol,
) {

    fun checkInterception() {
        val typeArg = expression.typeArguments.first()
        val type = typeArg.toConeTypeProjection().type ?: return
        checkInterceptionTypeParameter(type, typeArg)
        val classSymbol = type.toRegularClassSymbol(session) ?: return
        checkInterceptionModality(classSymbol, typeArg)
        checkInterceptionDefaultConstructor(classSymbol, typeArg)
        checkInterceptionFinalMembers(classSymbol, typeArg)
    }

    fun checkTemplating() {
        checkTemplatingFunctionalParam()
    }

    // checkInterception
    private fun checkInterceptionTypeParameter(type: ConeKotlinType, typeArg: FirTypeProjection) {
        if (type is ConeTypeParameterType) {
            reporter.reportOn(
                source = typeArg.source ?: expression.source,
                factory = MokkeryDiagnostics.INDIRECT_INTERCEPTION,
                a = funSymbol.name,
                b = type,
                context = context
            )
        }
    }

    private fun checkInterceptionModality(classSymbol: FirRegularClassSymbol, typeArg: FirTypeProjection) {
        val modalityDiagnostic = when (classSymbol.modality) {
            Modality.SEALED -> MokkeryDiagnostics.SEALED_TYPE_CANNOT_BE_INTERCEPTED
            Modality.FINAL -> MokkeryDiagnostics.FINAL_TYPE_CANNOT_BE_INTERCEPTED
            else -> null
        }
        if (modalityDiagnostic != null) {
            reporter.reportOn(
                source = typeArg.source ?: expression.source,
                factory = modalityDiagnostic,
                a = funSymbol.name,
                b = classSymbol.defaultType(),
                context = context
            )
        }
    }

    private fun checkInterceptionDefaultConstructor(classSymbol: FirRegularClassSymbol, typeArg: FirTypeProjection) {
        val constructors = classSymbol.constructors
        if (constructors.any() && constructors.none { it.isDefault() }) {
            reporter.reportOn(
                source = typeArg.source ?: expression.source,
                factory = MokkeryDiagnostics.NO_DEFAULT_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED,
                a = funSymbol.name,
                b = classSymbol.defaultType(),
                context = context
            )
        }
    }

    private fun checkInterceptionFinalMembers(classSymbol: FirRegularClassSymbol, typeArg: FirTypeProjection) {
        val inheritedSymbols = classSymbol
            .resolvedSuperTypes
            .asSequence()
            .mapNotNull { it.toRegularClassSymbol(session) }
            .flatMap { it.declarationSymbols }
        val allDeclarationSymbols = classSymbol
            .declarationSymbols
            .asSequence()
            .plus(inheritedSymbols)
        val finalDeclarations = allDeclarationSymbols
            .filter { it is FirCallableSymbol<*> && it !is FirConstructorSymbol && it.visibility != Visibilities.Private && it.isFinal }
            .toList()
        if (finalDeclarations.isNotEmpty()) {
            return reporter.reportOn(
                source = typeArg.source ?: expression.source,
                factory = MokkeryDiagnostics.FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED,
                a = funSymbol.name,
                b = classSymbol.defaultType(),
                c = finalDeclarations,
                context = context
            )
        }
    }

    // checkTemplating

    fun checkTemplatingFunctionalParam() {
        val blockArgument = expression
            .arguments
            .last()
            .unwrapArgument()
        if (blockArgument !is FirAnonymousFunctionExpression) {
            return reporter.reportOn(
                source = blockArgument.source ?: expression.source,
                factory = MokkeryDiagnostics.FUNCTIONAL_PARAM_MUST_BE_LAMBDA,
                a = funSymbol.name,
                b = funSymbol.valueParameterSymbols.last(),
                context = context
            )
        }
    }
}
