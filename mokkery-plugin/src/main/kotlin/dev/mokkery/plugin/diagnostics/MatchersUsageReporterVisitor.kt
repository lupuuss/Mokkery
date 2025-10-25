package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.Callable
import dev.mokkery.plugin.fir.KtDiagnosticsContainerCompat
import dev.mokkery.plugin.fir.acceptsMatcher
import dev.mokkery.plugin.fir.allNonDispatchArgumentsMapping
import dev.mokkery.plugin.fir.extractArrayLiteralCall
import dev.mokkery.plugin.fir.isSpread
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticContext
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirAnonymousObject
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.expressions.FirBooleanOperatorExpression
import org.jetbrains.kotlin.fir.expressions.FirDoWhileLoop
import org.jetbrains.kotlin.fir.expressions.FirEqualityOperatorCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirLoop
import org.jetbrains.kotlin.fir.expressions.FirTryExpression
import org.jetbrains.kotlin.fir.expressions.FirTypeOperatorCall
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.expressions.FirWhileLoop
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.calleeReference
import org.jetbrains.kotlin.fir.expressions.dispatchReceiver
import org.jetbrains.kotlin.fir.lastExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedVariableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirVariableSymbol
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.utils.addToStdlib.popLast
import org.jetbrains.kotlin.utils.memoryOptimizedMap

enum class MatchersUsageContext {
    BUILDER, TEMPLATING
}

class MatchersUsageReporterVisitor(
    private val session: FirSession,
    private val context: DiagnosticContext,
    private val reporter: DiagnosticReporter,
    private val configuration: CompilerConfiguration,
    private val parentFunction: FirFunctionSymbol<*>,
    private val usageContext: MatchersUsageContext,
    private val matchersProcessor: MatchersProcessor = MatchersProcessor(session)
) : FirVisitorVoid() {

    private val contextFunctions = setOf(Callable.ext, Callable.ctx)
    private val templatingFunctions = setOf(
        Callable.every,
        Callable.everySuspend,
        Callable.verify,
        Callable.verifySuspend
    )

    private val nestedClassStack = mutableListOf<FirClassSymbol<*>>()
    private val functionsStack = mutableListOf<FirFunctionSymbol<*>>()
    private val currentCallsStack = mutableListOf<FirFunctionCall>()
    private val callAssociatedLambdas = mutableMapOf<FirFunctionSymbol<*>, FirFunctionCall?>()
    private val legalizedNonMemberFunctionWithMatchers = mutableSetOf<FirFunctionCall>()

    override fun visitElement(element: FirElement) = element.acceptChildren(this)

    override fun visitAnonymousObject(anonymousObject: FirAnonymousObject) {
        nestedClassStack.add(anonymousObject.symbol)
        super.visitAnonymousObject(anonymousObject)
        nestedClassStack.popLast()
    }

    override fun visitRegularClass(regularClass: FirRegularClass) {
        visitClass(regularClass)
    }

    override fun visitClass(klass: FirClass) {
        nestedClassStack.add(klass.symbol)
        super.visitClass(klass)
        nestedClassStack.popLast()
    }

    override fun visitAnonymousFunction(anonymousFunction: FirAnonymousFunction) {
        functionsStack += anonymousFunction.symbol
        callAssociatedLambdas[anonymousFunction.symbol] = currentCallsStack.lastOrNull()
        super.visitAnonymousFunction(anonymousFunction)
        callAssociatedLambdas.remove(anonymousFunction.symbol)
        functionsStack -= anonymousFunction.symbol
    }

    override fun visitSimpleFunction(simpleFunction: FirSimpleFunction) {
        visitFunction(simpleFunction)
    }

    override fun visitFunction(function: FirFunction) {
        functionsStack += function.symbol
        super.visitFunction(function)
        functionsStack -= function.symbol
    }

    override fun visitProperty(property: FirProperty) {
        matchersProcessor.processVariable(property)
        super.visitProperty(property)
    }

    override fun visitVariableAssignment(variableAssignment: FirVariableAssignment) = context(context) {
        val variableSymbol = variableAssignment
            .calleeReference
            ?.toResolvedVariableSymbol()
            ?: return super.visitVariableAssignment(variableAssignment)
        if (variableAssignment.dispatchReceiver != null) return super.visitVariableAssignment(variableAssignment)
        if (!matchersProcessor.isMatcher(variableAssignment.rValue)) return super.visitVariableAssignment(variableAssignment)
        when (matchersProcessor.getResultFor(variableSymbol)) {
            null -> reporter.reportOn(
                source = variableAssignment.source,
                factory = Diagnostics.VARIABLE_OUT_OF_SCOPE,
                a = variableSymbol
            )
            is MatcherProcessingResult.RegularExpr -> reporter.reportOn(
                source = variableAssignment.source,
                factory = Diagnostics.VARIABLE_NOT_A_MATCHER,
                a = variableSymbol
            )
            else -> Unit
        }
        super.visitVariableAssignment(variableAssignment)
    }

    override fun visitWhenExpression(whenExpression: FirWhenExpression) = context(context) {
        val subjectInitializer = whenExpression.subjectVariable?.initializer
        if (subjectInitializer != null && matchersProcessor.isMatcher(subjectInitializer)) {
            reporter.reportOn(subjectInitializer.source, Diagnostics.ILLEGAL_MATCHER_IN_WHEN_SUBJECT)
        }
        whenExpression
            .branches
            .memoryOptimizedMap { it.condition }
            .forEachMatcher {
                reporter.reportOn(it.source, Diagnostics.ILLEGAL_MATCHER_IN_CONDITION)
            }
        super.visitWhenExpression(whenExpression)
    }

    override fun visitWhileLoop(whileLoop: FirWhileLoop) {
        visitLoop(whileLoop)    }

    override fun visitDoWhileLoop(doWhileLoop: FirDoWhileLoop) {
        visitLoop(doWhileLoop)
    }

    override fun visitLoop(loop: FirLoop) = context(context) {
        if (matchersProcessor.isMatcher(loop.condition)) {
            reporter.reportOn(loop.condition.source, Diagnostics.ILLEGAL_MATCHER_IN_CONDITION)
        }
        super.visitLoop(loop)
    }

    override fun visitTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall) = context(context) {
        typeOperatorCall.arguments.forEach {
            if (matchersProcessor.isMatcher(it)) {
                reporter.reportOn(it.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, typeOperatorCall.operation.operator)
            }
        }
        super.visitTypeOperatorCall(typeOperatorCall)
    }

    override fun visitEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall) = context(context) {
        equalityOperatorCall.arguments.forEach {
            if (matchersProcessor.isMatcher(it)) {
                reporter.reportOn(
                    it.source,
                    Diagnostics.ILLEGAL_OPERATOR_USAGE,
                    equalityOperatorCall.operation.operator
                )
            }
        }
        super.visitEqualityOperatorCall(equalityOperatorCall)
    }

    override fun visitBooleanOperatorExpression(
        booleanOperatorExpression: FirBooleanOperatorExpression
    ) = context(context) {
        listOf(booleanOperatorExpression.rightOperand, booleanOperatorExpression.leftOperand).forEach {
            if (matchersProcessor.isMatcher(it)) {
                reporter.reportOn(it.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, booleanOperatorExpression.kind.token)
            }
        }
        super.visitBooleanOperatorExpression(booleanOperatorExpression)
    }

    override fun visitTryExpression(tryExpression: FirTryExpression) = context(context) {
        if (matchersProcessor.isMatcher(tryExpression.tryBlock.lastExpression)) {
            reporter.reportOn(tryExpression.source, Diagnostics.ILLEGAL_TRY_CATCH)
        }
        tryExpression.catches.forEach { catch ->
            if (matchersProcessor.isMatcher(catch.block.lastExpression)) {
                reporter.reportOn(tryExpression.source, Diagnostics.ILLEGAL_TRY_CATCH)
            }
        }
        if (matchersProcessor.isMatcher(tryExpression.finallyBlock?.lastExpression)) {
            reporter.reportOn(tryExpression.source, Diagnostics.ILLEGAL_TRY_CATCH)
        }
        super.visitTryExpression(tryExpression)
    }

    override fun visitGetClassCall(getClassCall: FirGetClassCall) = context(context) {
        if (matchersProcessor.isMatcher(getClassCall.argument)) {
            reporter.reportOn(getClassCall.argument.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, "::class")
        }
        super.visitGetClassCall(getClassCall)
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall): Unit = context(context) {
        val callee = functionCall.calleeReference as? FirResolvedNamedReference
            ?: return super.visitFunctionCall(functionCall)
        val symbol = callee.resolvedSymbol as? FirFunctionSymbol<*>
            ?: return super.visitFunctionCall(functionCall)
        if (symbol.callableId in templatingFunctions) {
            reporter.reportOn(functionCall.source, Diagnostics.ILLEGAL_NESTED_TEMPLATING, symbol.name)
            return super.visitFunctionCall(functionCall)
        }
        val dispatchReceiver = functionCall.dispatchReceiver
        when {
            matchersProcessor.isMatcher(functionCall) && ensureCanUseMatchersNow(functionCall) -> {
                reportIllegalMatcherConstruction(functionCall, symbol)
            }
            dispatchReceiver != null && matchersProcessor.isMatcher(dispatchReceiver) -> {
                reporter.reportOn(dispatchReceiver.source, Diagnostics.ILLEGAL_METHOD_INVOCATION_ON_MATCHER)
            }
            dispatchReceiver != null && symbol !is FirConstructorSymbol -> {
                reportIllegalMatchersUsageWithMethods(functionCall, symbol)
            }
            symbol.callableId !in contextFunctions && functionCall !in legalizedNonMemberFunctionWithMatchers -> {
                reportIllegalMatchersUsageWithNonMemberFunctions(functionCall)
            }
        }
        currentCallsStack.add(functionCall)
        super.visitFunctionCall(functionCall)
        currentCallsStack.removeLast()
    }

    private fun ensureCanUseMatchersNow(call: FirFunctionCall): Boolean = context(context) {
        val currentParent = functionsStack.lastOrNull()
        val associatedCallToLambda = callAssociatedLambdas[currentParent]
        val associatedCalleeRef = associatedCallToLambda?.calleeReference as? FirResolvedNamedReference
        val associatedCallSymbol = associatedCalleeRef?.resolvedSymbol as? FirNamedFunctionSymbol
        if (nestedClassStack.isNotEmpty()) {
            reporter.reportOn(
                call.source,
                Diagnostics.ILLEGAL_NESTED_CLASS_MATCHERS,
                nestedClassStack.last()
            )
            return false
        }
        if (
            currentParent == null
            || currentParent == parentFunction
            || associatedCallSymbol?.callableId in contextFunctions
        ) {
            return true
        }
        reporter.reportOn(
            call.source,
            Diagnostics.ILLEGAL_NESTED_FUNCTIONS_MATCHERS,
            currentParent
        )
        return false
    }

    private fun reportIllegalMatcherConstruction(
        call: FirFunctionCall,
        symbol: FirFunctionSymbol<*>
    ) = context(context) {
        val argumentsMapping = call.allNonDispatchArgumentsMapping(symbol)
        for ((param, arg) in argumentsMapping) {
            if (!param.acceptsMatcher(session) && matchersProcessor.isMatcher(arg)) {
                reporter.reportOn(
                    source = arg.source,
                    factory = Diagnostics.MATCHER_PASSED_TO_NON_MATCHER_PARAM,
                    a = param
                )
            }
        }
        if (symbol.callableId == Callable.matches && symbol.valueParameterSymbols.size == 1) {
            val type = matchersProcessor.extractMatcherType(call)
            if (type == FirMatcherType.Composite) {
                reporter.reportOn(
                    call.arguments[0].source,
                    Diagnostics.MATCHES_WITH_COMPOSITE_ARG
                )
            }
        }
    }

    private fun reportIllegalMatchersUsageWithNonMemberFunctions(call: FirFunctionCall) = context(context) {
        val arguments = call.contextArguments
            .plus(call.extensionReceiver)
            .plus(call.arguments)
        arguments.forEachMatcher {
            reporter.reportOn(
                source = it.source,
                factory = Diagnostics.MATCHER_PASSED_TO_NON_MEMBER_FUNCTION
            )
        }
    }

    @OptIn(SymbolInternals::class)
    private fun reportIllegalMatchersUsageWithMethods(
        call: FirFunctionCall,
        symbol: FirFunctionSymbol<*>
    ) = context(context) {
        when (usageContext) {
            MatchersUsageContext.BUILDER -> reportIllegalMatchersUsageInMatcherBuilder(call)
            MatchersUsageContext.TEMPLATING -> when {
                symbol.isFinal -> reportIllegalFinalMethodMatchers(call, symbol)
                else -> reportIllegalMatchersInMockableMethod(call, symbol)
            }
        }
    }

    private fun reportIllegalMatchersUsageInMatcherBuilder(call: FirFunctionCall) = context(context) {
        val arguments = call.contextArguments
            .plus(call.extensionReceiver)
            .plus(call.arguments)
        arguments.forEachMatcher {
            reporter.reportOn(
                source = it.source,
                factory = Diagnostics.MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER
            )
        }
    }

    private fun reportIllegalFinalMethodMatchers(
        call: FirFunctionCall,
        symbol: FirFunctionSymbol<*>
    ) = context(context) {
        val arguments = call.contextArguments
            .plus(call.extensionReceiver)
            .plus(call.arguments)
        arguments.forEachMatcher {
            reporter.reportOn(
                source = it.source,
                factory = Diagnostics.MATCHER_USED_WITH_FINAL_METHOD,
                a = symbol
            )
        }
    }

    private fun reportIllegalMatchersInMockableMethod(
        call: FirFunctionCall,
        symbol: FirFunctionSymbol<*>
    ) = context(context) {
        for ((param, arg) in call.allNonDispatchArgumentsMapping(symbol)) {
            if (param is FirValueParameterSymbol && param.isVararg) {
                val vararg = arg as FirVarargArgumentsExpression
                reportIllegalVarargMatchers(call, vararg)
            }
        }
    }

    private fun reportIllegalVarargMatchers(
        call: FirFunctionCall,
        vararg: FirVarargArgumentsExpression
    ): Unit = context(context) {
        var varargMatchersCount = 0
        for (arg in vararg.arguments) {
            if (!arg.isSpread()) continue
            val arrayLiteralCall = arg.extractArrayLiteralCall(session)
            if (arrayLiteralCall != null) {
                legalizedNonMemberFunctionWithMatchers += arrayLiteralCall
                reportIllegalVarargMatchers(call, arrayLiteralCall.arguments[0] as FirVarargArgumentsExpression)
                continue
            }
            if (matchersProcessor.isMatcher(arg) && varargMatchersCount++ > 0) {
                reporter.reportOn(arg.source, Diagnostics.SINGLE_VARARG_MATCHER_ALLOWED)
            }
        }
    }

    private inline fun <T : FirExpression?> List<T>.forEachMatcher(block: (T & Any) -> Unit) {
        forEach {
            if (it != null && matchersProcessor.isMatcher(it)) block(it)
        }
    }

    object Diagnostics : KtDiagnosticsContainerCompat() {

        override fun getRendererFactory() = MatchersUsageDiagnosticRendererFactory()

        val MATCHER_PASSED_TO_NON_MATCHER_PARAM by error1<KtElement, FirBasedSymbol<*>>()
        val ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION by error1<KtElement, FirCallableSymbol<*>>()
        val ILLEGAL_METHOD_INVOCATION_ON_MATCHER by error0<KtElement>()
        val ILLEGAL_OPERATOR_USAGE by error1<KtElement, String>()
        val ILLEGAL_TRY_CATCH by error0<KtElement>()
        val ILLEGAL_NESTED_TEMPLATING by error1<KtElement, Name>()
        val ILLEGAL_NESTED_FUNCTIONS_MATCHERS by error1<KtElement, FirFunctionSymbol<*>>()
        val ILLEGAL_NESTED_CLASS_MATCHERS by error1<KtElement, FirClassSymbol<*>>()
        val ILLEGAL_MATCHER_IN_CONDITION by error0<KtElement>()
        val ILLEGAL_MATCHER_IN_WHEN_SUBJECT by error0<KtElement>()
        val VARIABLE_OUT_OF_SCOPE by error1<KtElement, FirVariableSymbol<*>>()
        val VARIABLE_NOT_A_MATCHER by error1<KtElement, FirVariableSymbol<*>>()
        val MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER by error0<KtElement>()
        val MATCHER_PASSED_TO_NON_MEMBER_FUNCTION by error0<KtElement>()
        val SINGLE_VARARG_MATCHER_ALLOWED by error0<KtElement>()
        val MATCHER_USED_WITH_FINAL_METHOD by error1<KtElement, FirFunctionSymbol<*>>()
        val MATCHES_WITH_COMPOSITE_ARG by error0<KtElement>()
    }
}
