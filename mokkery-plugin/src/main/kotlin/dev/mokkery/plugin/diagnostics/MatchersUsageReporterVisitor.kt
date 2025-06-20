package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.Callable
import dev.mokkery.plugin.fir.KtDiagnosticsContainerCompat
import dev.mokkery.plugin.fir.acceptsMatcher
import dev.mokkery.plugin.fir.allNonDispatchArgumentsMapping
import dev.mokkery.plugin.fir.extractArrayLiteralCall
import dev.mokkery.plugin.fir.isSpread
import dev.mokkery.plugin.fir.isVarargMatcher
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
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirLoop
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
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
        val rValueTypes = matchersProcessor.extractAllMatcherTypes(variableAssignment.rValue).toList()
        if (rValueTypes.isEmpty()) return super.visitVariableAssignment(variableAssignment)
        when (val type = matchersProcessor.getResultFor(variableSymbol)) {
            null -> reporter.reportOn(
                source = variableAssignment.source,
                factory = Diagnostics.VARIABLE_OUT_OF_SCOPE,
                a = variableSymbol
            )
            is MatcherProcessingResult.RegularExpression -> reporter.reportOn(
                source = variableAssignment.source,
                factory = Diagnostics.VARIABLE_NOT_A_MATCHER,
                a = variableSymbol
            )
            is MatcherProcessingResult.MatcherExpression -> {
                val isAssignedWithVarargMatcher = rValueTypes.any { it.isVararg }
                if (isAssignedWithVarargMatcher != type.matcherType.isVararg) {
                    reporter.reportOn(
                        source = variableAssignment.source,
                        factory = Diagnostics.INCOMPATIBLE_VARIABLE_TYPE,
                        a = variableSymbol,
                        b = if (type.matcherType.isVararg) "vararg" else "regular",
                        c = if (isAssignedWithVarargMatcher) "vararg" else "regular",
                    )
                }
            }
        }
        super.visitVariableAssignment(variableAssignment)
    }

    override fun visitWhenExpression(whenExpression: FirWhenExpression) = context(context) {
        var varargsCount = 0
        var otherMatchersCount = 0
        var nonMatchersExpressions = 0
        val subjectInitializer = whenExpression.subjectVariable?.initializer
        if (subjectInitializer != null && matchersProcessor.isMatcher(subjectInitializer)) {
            reporter.reportOn(subjectInitializer.source, Diagnostics.ILLEGAL_MATCHER_IN_WHEN_SUBJECT)
        }
        for (branch in whenExpression.branches) {
            if (matchersProcessor.isMatcher(branch.condition)) {
                reporter.reportOn(branch.condition.source, Diagnostics.ILLEGAL_MATCHER_IN_CONDITION)
            }
            val branchExpression = branch.result.lastExpression ?: continue
            val matchersCalls = matchersProcessor.extractAllMatcherTypes(branchExpression).toList()
            when {
                matchersCalls.isEmpty() -> nonMatchersExpressions++
                matchersCalls.any { it.isVararg } -> varargsCount++
                else -> otherMatchersCount++
            }
        }
        if (varargsCount == 0) {
            return super.visitWhenExpression(whenExpression)
        }
        if (nonMatchersExpressions != 0 || otherMatchersCount != 0) {
            for (branch in whenExpression.branches) {
                val branchExpression = branch.result.lastExpression ?: continue
                if (!matchersProcessor.isVarargMatcher(branchExpression)) {
                    reporter.reportOn(
                        source = branch.source,
                        factory = Diagnostics.VARARG_REQUIRED_IN_ALL_BRANCHES
                    )
                }
            }
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

    override fun visitReturnExpression(returnExpression: FirReturnExpression) = context(context) {
        if (usageContext != MatchersUsageContext.BUILDER) return super.visitReturnExpression(returnExpression)
        if (returnExpression.target.labeledElement.symbol != parentFunction) {
            return super.visitReturnExpression(returnExpression)
        }
        val isVarargBuilderAnnotated = parentFunction.isVarargMatcher(session)
        val usesVarargMatchers = matchersProcessor.isVarargMatcher(returnExpression.result)
        when {
            usesVarargMatchers && !isVarargBuilderAnnotated -> {
                reporter.reportOn(parentFunction.source, Diagnostics.MATCHER_RETURNING_VARARG_MATCHER_MUST_BE_ANNOTATED)
            }
            isVarargBuilderAnnotated && !usesVarargMatchers -> {
                reporter.reportOn(returnExpression.source, Diagnostics.VARARG_MATCHER_BUILDER_MUST_RETURN_VARARG_MATCHERS_ONLY)
            }
        }
        super.visitReturnExpression(returnExpression)
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
        var regularMatchers = 0
        var varargMatchers = 0
        val argumentsMapping = call.allNonDispatchArgumentsMapping(symbol)
        for ((param, arg) in argumentsMapping) {
            when {
                param.acceptsMatcher(session) -> {
                    if (matchersProcessor.isVarargMatcher(arg)) varargMatchers++ else regularMatchers++
                }
                matchersProcessor.isMatcher(arg) -> reporter.reportOn(
                    source = arg.source,
                    factory = Diagnostics.MATCHER_PASSED_TO_NON_MATCHER_PARAM,
                    a = param
                )
            }
        }
        if (varargMatchers > 0 && regularMatchers > 0) {
            argumentsMapping.forEach { (param, arg) ->
                if (param.acceptsMatcher(session) && !matchersProcessor.isVarargMatcher(arg)) {
                    reporter.reportOn(arg.source, Diagnostics.ILLEGAL_VARARGS_COMPOSITE)
                }
            }
        }
        if (symbol.callableId == Callable.matches) {
            val type = matchersProcessor.extractMatcherType(call)
            if (type?.isComposite == true) {
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
        arguments.forEach {
            if (it != null && matchersProcessor.isMatcher(it)) {
                reporter.reportOn(
                    source = it.source,
                    factory = Diagnostics.MATCHER_PASSED_TO_NON_MEMBER_FUNCTION
                )
            }
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
        arguments.forEach {
            if (it != null && matchersProcessor.isMatcher(it)) {
                reporter.reportOn(
                    source = it.source,
                    factory = Diagnostics.MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER
                )
            }
        }
    }

    private fun reportIllegalFinalMethodMatchers(
        call: FirFunctionCall,
        symbol: FirFunctionSymbol<*>
    ) = context(context) {
        val arguments = call.contextArguments
            .plus(call.extensionReceiver)
            .plus(call.arguments)
        arguments.forEach {
            if (it != null && matchersProcessor.isMatcher(it)) {
                reporter.reportOn(
                    source = it.source,
                    factory = Diagnostics.MATCHER_USED_WITH_FINAL_METHOD,
                    a = symbol
                )
            }
        }
    }

    private fun reportIllegalMatchersInMockableMethod(call: FirFunctionCall, symbol: FirFunctionSymbol<*>) = context(context) {
        for ((param, arg) in call.allNonDispatchArgumentsMapping(symbol)) {
            when {
                param is FirValueParameterSymbol && param.isVararg -> {
                    val vararg = arg as FirVarargArgumentsExpression
                    reportIllegalVarargMatchers(call, vararg)
                }
                matchersProcessor.isVarargMatcher(arg) -> reporter.reportOn(
                    source = arg.source,
                    factory = Diagnostics.VARARG_MATCHER_WITHOUT_VARARG
                )
            }
        }
    }

    private fun reportIllegalVarargMatchers(
        call: FirFunctionCall,
        vararg: FirVarargArgumentsExpression
    ): Unit = context(context) {
        var varargMatchersCount = 0
        for (arg in vararg.arguments) {
            when {
                arg.isSpread() -> {
                    val arrayLiteralCall = arg.extractArrayLiteralCall(session)
                    if (arrayLiteralCall != null) {
                        legalizedNonMemberFunctionWithMatchers += arrayLiteralCall
                        reportIllegalVarargMatchers(call, arrayLiteralCall.arguments[0] as FirVarargArgumentsExpression)
                        continue
                    }
                    val type = matchersProcessor.extractMatcherType(arg)
                    when {
                        type == null -> continue
                        type.isVararg -> if (varargMatchersCount++ > 0) {
                            reporter.reportOn(arg.source, Diagnostics.SINGLE_VARARG_MATCHER_ALLOWED)
                        }
                        else -> reporter.reportOn(arg.source, Diagnostics.ILLEGAL_SPREAD_FOR_VARARG)
                    }
                }
                matchersProcessor.isVarargMatcher(arg) -> {
                    reporter.reportOn(arg.source, Diagnostics.VARARG_MATCHER_USED_WITHOUT_SPREAD)
                }
            }
        }
    }

    object Diagnostics : KtDiagnosticsContainerCompat() {

        override fun getRendererFactory() = MatchersUsageDiagnosticRendererFactory()

        val MATCHER_PASSED_TO_NON_MATCHER_PARAM by error1<KtElement, FirBasedSymbol<*>>()
        val ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION by error1<KtElement, FirCallableSymbol<*>>()
        val ILLEGAL_METHOD_INVOCATION_ON_MATCHER by error0<KtElement>()
        val ILLEGAL_VARARGS_COMPOSITE by error0<KtElement>()
        val ILLEGAL_OPERATOR_USAGE by error1<KtElement, String>()
        val ILLEGAL_TRY_CATCH by error0<KtElement>()
        val VARARG_REQUIRED_IN_ALL_BRANCHES by error0<KtElement>()
        val ILLEGAL_NESTED_TEMPLATING by error1<KtElement, Name>()
        val ILLEGAL_NESTED_FUNCTIONS_MATCHERS by error1<KtElement, FirFunctionSymbol<*>>()
        val ILLEGAL_NESTED_CLASS_MATCHERS by error1<KtElement, FirClassSymbol<*>>()
        val ILLEGAL_MATCHER_IN_CONDITION by error0<KtElement>()
        val ILLEGAL_MATCHER_IN_WHEN_SUBJECT by error0<KtElement>()
        val VARIABLE_OUT_OF_SCOPE by error1<KtElement, FirVariableSymbol<*>>()
        val VARIABLE_NOT_A_MATCHER by error1<KtElement, FirVariableSymbol<*>>()
        val INCOMPATIBLE_VARIABLE_TYPE by error3<KtElement, FirVariableSymbol<*>, String, String>()
        val MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER by error0<KtElement>()
        val MATCHER_PASSED_TO_NON_MEMBER_FUNCTION by error0<KtElement>()
        val ILLEGAL_SPREAD_FOR_VARARG by error0<KtElement>()
        val SINGLE_VARARG_MATCHER_ALLOWED by error0<KtElement>()
        val VARARG_MATCHER_WITHOUT_VARARG by error0<KtElement>()
        val VARARG_MATCHER_USED_WITHOUT_SPREAD by error0<KtElement>()
        val MATCHER_RETURNING_VARARG_MATCHER_MUST_BE_ANNOTATED by error0<KtElement>()
        val VARARG_MATCHER_BUILDER_MUST_RETURN_VARARG_MATCHERS_ONLY by error0<KtElement>()
        val MATCHER_USED_WITH_FINAL_METHOD by error1<KtElement, FirFunctionSymbol<*>>()
        val MATCHES_WITH_COMPOSITE_ARG by error0<KtElement>()
    }
}
