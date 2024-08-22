package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.MembersValidationMode
import dev.mokkery.plugin.core.Mokkery.Callable
import dev.mokkery.plugin.core.validationMode
import dev.mokkery.plugin.fir.constructors
import org.jetbrains.kotlin.AbstractKtSourceElement
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.declarations.utils.isInline
import org.jetbrains.kotlin.fir.declarations.utils.isInterface
import org.jetbrains.kotlin.fir.declarations.utils.modality
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.isPrimitiveType
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
import org.jetbrains.kotlin.fir.types.isSomeFunctionType
import org.jetbrains.kotlin.fir.types.toConeTypeProjection
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm

class MokkeryCallsChecker(
    private val session: FirSession,
    configuration: CompilerConfiguration,
) : FirFunctionCallChecker(MppCheckerKind.Common) {
    private val mock = Callable.mock
    private val mockMany = Callable.mockMany
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
            context = context,
            reporter = reporter,
            funSymbol = symbol,
            validationMode = validationMode
        )
        when (symbol.callableId) {
            mock, spy -> scope.checkInterception(expression)
            mockMany -> scope.checkManyInterceptions(expression)
            every, everySuspend, verify, verifySuspend -> scope.checkTemplating(expression)
        }
    }
}

private class MokkeryScopedCallsChecker(
    private val session: FirSession,
    private val validationMode: MembersValidationMode,
    private val context: CheckerContext,
    private val reporter: DiagnosticReporter,
    private val funSymbol: FirNamedFunctionSymbol,
) {

    private val isWasm = session.moduleData.platform.isWasm()
    private val wasmHashCode = Name.identifier("_hashCode")
    private val wasmTypeInfo = Name.identifier("typeInfo")
    private val wasmSpecialPropertyNames = listOf(wasmHashCode, wasmTypeInfo)

    fun checkManyInterceptions(expression: FirFunctionCall) {
        val classMappings = expression.typeArguments.groupBy {
            val type = it.toConeTypeProjection().type ?: return
            if (!checkInterceptionType(it.source ?: expression.source, type)) return
            val classSymbol = type.toRegularClassSymbol(session) ?: return
            classSymbol
        }
        if (!checkNoDuplicates(expression.typeArguments.size, classMappings)) return
        if (!checkOneSuperClass(classMappings)) return
        if (!checkJsFunctionalTypes(classMappings)) return
    }

    fun checkInterception(expression: FirFunctionCall): Boolean {
        val typeArg = expression.typeArguments.first()
        val type = typeArg.toConeTypeProjection().type ?: return false
        val source = typeArg.source ?: expression.source
        return checkInterceptionType(source, type)
    }

    fun checkTemplating(expression: FirFunctionCall) {
        val blockArgument = expression
            .arguments
            .last()
            .unwrapArgument()
        checkTemplatingFunctionalParam(blockArgument.source ?: expression.source, blockArgument)
    }

    private fun checkNoDuplicates(
        argumentsCount: Int,
        classMapping: Map<FirRegularClassSymbol, List<FirTypeProjection>>
    ): Boolean {
        if (classMapping.size == argumentsCount) return true
        val entry = classMapping.entries.first { it.value.size > 1 }
        reporter.reportOn(
            source = entry.value[1].source,
            factory = MokkeryDiagnostics.DUPLICATE_TYPES_FOR_MOCK_MANY,
            a = entry.key.defaultType(),
            b = funSymbol.name,
            c = entry.value.size.toString(),
            context = context
        )
        return false
    }

    private fun checkOneSuperClass(classMapping: Map<FirRegularClassSymbol, List<FirTypeProjection>>): Boolean {
        val regularClasses = classMapping.keys.filter { it.classKind == ClassKind.CLASS }
        if (regularClasses.size <= 1) return true
        reporter.reportOn(
            source = classMapping.getValue(regularClasses[1]).first().source,
            factory = MokkeryDiagnostics.MULTIPLE_SUPER_CLASSES_FOR_MOCK_MANY,
            a = funSymbol.name,
            b = regularClasses.map { it.defaultType() },
            context = context
        )
        return false
    }

    private fun checkJsFunctionalTypes(classMapping: Map<FirRegularClassSymbol, List<FirTypeProjection>>): Boolean {
        if (!session.moduleData.platform.isJs()) return true
        val funClass = classMapping.keys.find { it.defaultType().isSomeFunctionType(session) } ?: return true
        reporter.reportOn(
            source = classMapping.getValue(funClass).first().source,
            factory = MokkeryDiagnostics.FUNCTIONAL_TYPE_ON_JS_FOR_MOCK_MANY,
            a = classMapping.getValue(funClass).first().toConeTypeProjection().type!!,
            b = funSymbol.name,
            context = context
        )
        return false
    }

    // checkInterception

    private fun checkInterceptionType(source: AbstractKtSourceElement?, type: ConeKotlinType): Boolean {
        if (!checkInterceptionTypeParameter(source, type)) return false
        val classSymbol = type.toRegularClassSymbol(session) ?: return false
        if (!checkInterceptionModality(source, classSymbol)) return false
        if (classSymbol.isInterface) return true
        if (!checkClassInterceptionRequirements(source, classSymbol)) return false
        return true
    }

    private fun checkInterceptionTypeParameter(source: AbstractKtSourceElement?, type: ConeKotlinType): Boolean {
        if (type !is ConeTypeParameterType) return true
        reporter.reportOn(
            source = source,
            factory = MokkeryDiagnostics.INDIRECT_INTERCEPTION,
            a = funSymbol.name,
            b = type,
            context = context
        )
        return false
    }

    private fun checkInterceptionModality(
        source: AbstractKtSourceElement?,
        classSymbol: FirRegularClassSymbol
    ): Boolean {
        val modality = classSymbol.modality
        val modalityDiagnostic = when  {
            classSymbol.isPrimitiveType() -> MokkeryDiagnostics.PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED
            modality == Modality.SEALED -> MokkeryDiagnostics.SEALED_TYPE_CANNOT_BE_INTERCEPTED
            modality == Modality.FINAL -> MokkeryDiagnostics.FINAL_TYPE_CANNOT_BE_INTERCEPTED
            else -> null
        }
        if (modalityDiagnostic == null) return true
        reporter.reportOn(
            source = source,
            factory = modalityDiagnostic,
            a = funSymbol.name,
            b = classSymbol.defaultType(),
            context = context
        )
        return false
    }

    private fun checkClassInterceptionRequirements(
        source: AbstractKtSourceElement?,
        classSymbol: FirRegularClassSymbol
    ): Boolean {
        val constructors = classSymbol.constructors
        if (constructors.none { it.visibility.isPublicAPI }) {
            reporter.reportOn(
                source = source,
                factory = MokkeryDiagnostics.NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED,
                a = funSymbol.name,
                b = classSymbol.defaultType(),
                context = context
            )
            return false
        }
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
        if (finalDeclarations.isEmpty()) return true
        reporter.reportOn(
            source = source,
            factory = MokkeryDiagnostics.FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED,
            a = funSymbol.name,
            b = classSymbol.defaultType(),
            c = finalDeclarations,
            context = context
        )
        return false
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

    private val FirCallableSymbol<*>.isInlineOrInlineProperty: Boolean
        get() {
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

    private fun checkTemplatingFunctionalParam(source: AbstractKtSourceElement?, blockArgument: FirExpression) {
        if (blockArgument !is FirAnonymousFunctionExpression) {
            return reporter.reportOn(
                source = source,
                factory = MokkeryDiagnostics.FUNCTIONAL_PARAM_MUST_BE_LAMBDA,
                a = funSymbol.name,
                b = funSymbol.valueParameterSymbols.last(),
                context = context
            )
        }
    }
}
