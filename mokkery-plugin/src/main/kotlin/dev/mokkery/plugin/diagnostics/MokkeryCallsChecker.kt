package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.MembersValidationMode
import dev.mokkery.plugin.core.Mokkery.Callable
import dev.mokkery.plugin.core.validationMode
import dev.mokkery.plugin.fir.constructors
import dev.mokkery.plugin.fir.isDefault
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.declarations.utils.isInterface
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.types.toConeTypeProjection
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.isWasm

class MokkeryCallsChecker(
    private val session: FirSession,
    configuration: CompilerConfiguration,
) : FirFunctionCallChecker() {

    private val mock = Callable.mock
    private val spy = Callable.spy
    private val every = Callable.every
    private val everySuspend = Callable.everySuspend
    private val verify = Callable.verify
    private val verifySuspend = Callable.verifySuspend

    private val validationMode = configuration.validationMode

    override fun check(expression: FirFunctionCall, context: CheckerContext, reporter: DiagnosticReporter) {
        val callee = expression.calleeReference as? FirResolvedNamedReference ?: return
        val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return
        val scope = MokkeryScopedCallsChecker(
            session = session,
            expression = expression,
            context = context,
            reporter = reporter,
            funSymbol = symbol,
            validationMode = validationMode
        )
        when (symbol.callableId) {
            mock, spy -> scope.checkInterception()
            every, everySuspend, verify, verifySuspend -> scope.checkTemplating()
        }
    }
}

private class MokkeryScopedCallsChecker(
    private val session: FirSession,
    private val validationMode: MembersValidationMode,
    private val expression: FirFunctionCall,
    private val context: CheckerContext,
    private val reporter: DiagnosticReporter,
    private val funSymbol: FirNamedFunctionSymbol,
) {

    private val isWasm = session.moduleData.platform.isWasm()
    private val wasmHashCode = Name.identifier("_hashCode")
    private val wasmTypeInfo = Name.identifier("typeInfo")
    private val wasmSpecialPropertyNames = listOf(wasmHashCode, wasmTypeInfo)

    fun checkInterception() {
        val typeArg = expression.typeArguments.first()
        val type = typeArg.toConeTypeProjection().type ?: return
        checkInterceptionTypeParameter(type, typeArg)
        val classSymbol = type.toRegularClassSymbol(session) ?: return
        checkInterceptionModality(classSymbol, typeArg)
        if (classSymbol.isInterface) return
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
            .filterOutWasmSpecialProperties() // TODO Remove when not detectable by FIR
            .filterNot { it.isValid(validationMode) }
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

    private fun FirBasedSymbol<*>.isValid(validationMode: MembersValidationMode): Boolean {
        if (this !is FirCallableSymbol<*>) return true
        if (this is FirConstructorSymbol) return true
        if (visibility == Visibilities.Private) return true
        if (!isFinal) return true
        return when (validationMode) {
            MembersValidationMode.Strict -> false
            MembersValidationMode.IgnoreInline -> isInlineOrInlineProperty
            MembersValidationMode.IgnoreFinal -> true
        }
    }

    private val FirCallableSymbol<*>.isInlineOrInlineProperty: Boolean get() {
        if (this !is FirPropertySymbol) return isInline
        val getter = getterSymbol
        val setter = setterSymbol
        return (getter == null || getter.isInline) && (setter == null || setter.isInline)
    }

    private fun Sequence<FirBasedSymbol<*>>.filterOutWasmSpecialProperties(): Sequence<FirBasedSymbol<*>> {
        if (!isWasm) return this
        return filterNot { it is FirPropertySymbol && it.name in wasmSpecialPropertyNames }
    }

    // checkTemplating

    private fun checkTemplatingFunctionalParam() {
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
