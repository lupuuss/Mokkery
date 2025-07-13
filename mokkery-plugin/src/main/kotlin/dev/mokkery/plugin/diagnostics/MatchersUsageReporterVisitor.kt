package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.Callable
import dev.mokkery.plugin.fir.acceptsMatcher
import dev.mokkery.plugin.fir.allNonDispatchArgumentsMapping
import dev.mokkery.plugin.fir.extractArrayLiteralCall
import dev.mokkery.plugin.fir.isMatcher
import dev.mokkery.plugin.fir.isSpread
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticContext
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.error3
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirAnonymousObject
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.FirBooleanOperatorExpression
import org.jetbrains.kotlin.fir.expressions.FirEqualityOperatorCall
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirTryExpression
import org.jetbrains.kotlin.fir.expressions.FirTypeOperatorCall
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.calleeReference
import org.jetbrains.kotlin.fir.expressions.dispatchReceiver
import org.jetbrains.kotlin.fir.lastExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedVariableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
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
    private val usageContext: MatchersUsageContext
) : FirVisitorVoid() {

    private val contextFunctions = setOf(Callable.ext, Callable.ctx)
    private val templatingFunctions = setOf(
        Callable.every,
        Callable.everySuspend,
        Callable.verify,
        Callable.verifySuspend
    )

    private val matchersProcessor = MatchersProcessor(session)

    private val nestedClassStack = mutableListOf<FirClassSymbol<*>>()
    private val functionsStack = mutableListOf<FirFunctionSymbol<*>>()
    private val currentCallsStack = mutableListOf<FirFunctionCall>()
    private val callAssociatedLambdas = mutableMapOf<FirFunctionSymbol<*>, FirFunctionCall?>()
    private val legalizedNonMemberFunctionWithMatchers = mutableSetOf<FirFunctionCall>()

    override fun visitElement(element: FirElement) = element.acceptChildren(this)

    override fun visitAnonymousObject(anonymousObject: FirAnonymousObject) {
        nestedClassStack.add(anonymousObject.symbol)
        anonymousObject.acceptChildren(this)
        nestedClassStack.popLast()
    }

    override fun visitClass(klass: FirClass) {
        nestedClassStack.add(klass.symbol)
        klass.acceptChildren(this)
        nestedClassStack.popLast()
    }

    override fun visitAnonymousFunction(anonymousFunction: FirAnonymousFunction) {
        functionsStack += anonymousFunction.symbol
        callAssociatedLambdas[anonymousFunction.symbol] = currentCallsStack.lastOrNull()
        anonymousFunction.acceptChildren(this)
        callAssociatedLambdas.remove(anonymousFunction.symbol)
        functionsStack -= anonymousFunction.symbol
    }

    override fun visitFunction(function: FirFunction) {
        functionsStack += function.symbol
        function.acceptChildren(this)
        functionsStack -= function.symbol
    }

    override fun visitProperty(property: FirProperty) {
        matchersProcessor.processVariable(property)
        property.acceptChildren(this)
    }

    override fun visitVariableAssignment(variableAssignment: FirVariableAssignment) = context(context) {
        val variableSymbol = variableAssignment
            .calleeReference
            ?.toResolvedVariableSymbol()
            ?: return variableAssignment.acceptChildren(this)
        if (variableAssignment.dispatchReceiver != null) return variableAssignment.acceptChildren(this)
        val rValueTypes = matchersProcessor.extractMatcherTypes(variableAssignment.rValue).toList()
        if (rValueTypes.isEmpty()) return variableAssignment.acceptChildren(this)
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
        variableAssignment.acceptChildren(this)
    }

    override fun visitWhenExpression(whenExpression: FirWhenExpression) = context(context) {
        var varargsCount = 0
        var otherMatchersCount = 0
        var nonMatchersExpressions = 0
        for (branch in whenExpression.branches) {
            if (matchersProcessor.usesMatchers(branch.condition)) {
                reporter.reportOn(branch.condition.source, Diagnostics.ILLEGAL_MATCHER_IN_CONDITION)
            }
            val branchExpression = branch.result.lastExpression
            if (branchExpression == null) continue
            val matchersCalls = matchersProcessor.extractMatcherTypes(branchExpression).toList()
            when {
                matchersCalls.isEmpty() -> nonMatchersExpressions++
                matchersCalls.any { it.isVararg } -> varargsCount++
                else -> otherMatchersCount++
            }
        }
        if (varargsCount == 0) {
            return whenExpression.acceptChildren(this)
        }
        if (nonMatchersExpressions != 0 || otherMatchersCount != 0) {
            for (branch in whenExpression.branches) {
                val branchExpression = branch.result.lastExpression
                if (branchExpression == null) continue
                if (!matchersProcessor.usesVarargMatchers(branchExpression)) {
                    reporter.reportOn(
                        source = branch.source,
                        factory = Diagnostics.VARARG_REQUIRED_IN_ALL_BRANCHES
                    )
                }
            }
        }
        whenExpression.acceptChildren(this)
    }

    override fun visitTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall) = context(context) {
        typeOperatorCall.arguments.forEach {
            if (matchersProcessor.usesMatchers(it)) {
                reporter.reportOn(it.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, typeOperatorCall.operation.operator)
            }
        }
        typeOperatorCall.acceptChildren(this)
    }

    override fun visitEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall) = context(context) {
        equalityOperatorCall.arguments.forEach {
            if (matchersProcessor.usesMatchers(it)) {
                reporter.reportOn(
                    it.source,
                    Diagnostics.ILLEGAL_OPERATOR_USAGE,
                    equalityOperatorCall.operation.operator
                )
            }
        }
        equalityOperatorCall.acceptChildren(this)
    }

    override fun visitBooleanOperatorExpression(
        booleanOperatorExpression: FirBooleanOperatorExpression
    ) = context(context) {
        listOf(booleanOperatorExpression.rightOperand, booleanOperatorExpression.leftOperand).forEach {
            if (matchersProcessor.usesMatchers(it)) {
                reporter.reportOn(it.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, booleanOperatorExpression.kind.token)
            }
        }
        booleanOperatorExpression.acceptChildren(this)
    }

    override fun visitTryExpression(tryExpression: FirTryExpression) = context(context) {
        if (matchersProcessor.usesMatchers(tryExpression.tryBlock.lastExpression)) {
            reporter.reportOn(tryExpression.source, Diagnostics.ILLEGAL_TRY_CATCH)
        }
        tryExpression.catches.forEach { catch ->
            if (matchersProcessor.usesMatchers(catch.block.lastExpression)) {
                reporter.reportOn(tryExpression.source, Diagnostics.ILLEGAL_TRY_CATCH)
            }
        }
        if (matchersProcessor.usesMatchers(tryExpression.finallyBlock?.lastExpression)) {
            reporter.reportOn(tryExpression.source, Diagnostics.ILLEGAL_TRY_CATCH)
        }
        tryExpression.acceptChildren(this)
    }

    override fun visitGetClassCall(getClassCall: FirGetClassCall) = context(context) {
        if (matchersProcessor.usesMatchers(getClassCall.argument)) {
            reporter.reportOn(getClassCall.argument.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, "::class")
        }
        getClassCall.acceptChildren(this)
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall): Unit = context(context) {
        val callee = functionCall.calleeReference as? FirResolvedNamedReference
            ?: return functionCall.acceptChildren(this)
        val symbol = callee.resolvedSymbol as? FirFunctionSymbol<*>
            ?: return functionCall.acceptChildren(this)
        if (symbol.callableId in templatingFunctions) {
            reporter.reportOn(functionCall.source, Diagnostics.ILLEGAL_NESTED_TEMPLATING, symbol.name)
            return functionCall.acceptChildren(this)
        }
        val dispatchReceiver = functionCall.dispatchReceiver
        when {
            functionCall.isMatcher() && ensureCanUseMatchersNow(functionCall) -> {
                reportIllegalMatcherConstruction(functionCall, symbol)
            }
            dispatchReceiver != null && matchersProcessor.usesMatchers(dispatchReceiver) -> {
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
        functionCall.acceptChildren(this)
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
        var regularMatchers = 0
        var varargMatchers = 0
        val argumentsMapping = call.allNonDispatchArgumentsMapping(symbol)
        for ((param, arg) in argumentsMapping) {
            when {
                param.acceptsMatcher(session) -> {
                    if (matchersProcessor.usesVarargMatchers(arg)) varargMatchers++ else regularMatchers++
                }
                matchersProcessor.usesMatchers(arg) -> reporter.reportOn(
                    source = arg.source,
                    factory = Diagnostics.MATCHER_PASSED_TO_NON_MATCHER_PARAM,
                    a = param
                )
            }
        }
        if (varargMatchers > 0 && regularMatchers > 0) {
            argumentsMapping.forEach { (param, arg) ->
                if (param.acceptsMatcher(session) && !matchersProcessor.usesVarargMatchers(arg)) {
                    reporter.reportOn(arg.source, Diagnostics.ILLEGAL_VARARGS_COMPOSITE)
                }
            }
        }
    }

    private fun reportIllegalMatchersUsageWithNonMemberFunctions(call: FirFunctionCall) = context(context) {
        val arguments = call.contextArguments
            .plus(call.extensionReceiver)
            .plus(call.arguments)
        arguments.forEach {
            if (it != null && matchersProcessor.usesMatchers(it)) {
                reporter.reportOn(
                    source = it.source,
                    factory = Diagnostics.MATCHER_PASSED_TO_NON_MEMBER_FUNCTION
                )
            }
        }
    }

    private fun reportIllegalMatchersUsageWithMethods(
        call: FirFunctionCall,
        symbol: FirFunctionSymbol<*>
    ) = context(context) {
        when (usageContext) {
            MatchersUsageContext.BUILDER -> {
                val arguments = call.contextArguments
                    .plus(call.extensionReceiver)
                    .plus(call.arguments)
                arguments.forEach {
                    if (it != null && matchersProcessor.usesMatchers(it)) {
                        reporter.reportOn(
                            source = it.source,
                            factory = Diagnostics.MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER
                        )
                    }
                }
            }
            MatchersUsageContext.TEMPLATING -> {
                for ((param, arg) in call.allNonDispatchArgumentsMapping(symbol)) {
                    when {
                        param is FirValueParameterSymbol && param.isVararg -> {
                            (arg as FirVarargArgumentsExpression).reportIllegalVarargMatchers(call)
                        }
                        matchersProcessor.usesVarargMatchers(arg) -> reporter.reportOn(
                            source = arg.source,
                            factory = Diagnostics.VARARG_MATCHER_WITHOUT_VARARG
                        )
                    }
                }
            }
        }
    }

    private fun FirVarargArgumentsExpression.reportIllegalVarargMatchers(call: FirFunctionCall): Unit = context(context) {
        var varargMatchersCount = 0
        for (arg in arguments) {
            when {
                arg.isSpread() -> {
                    val arrayLiteralCall = arg.extractArrayLiteralCall(session)
                    if (arrayLiteralCall != null) {
                        legalizedNonMemberFunctionWithMatchers += arrayLiteralCall
                        arrayLiteralCall.arguments[0]
                            .let { it as FirVarargArgumentsExpression }
                            .reportIllegalVarargMatchers(call)
                        continue
                    }
                    val type = matchersProcessor.extractMatcherSingleType(arg)
                    when {
                        type == null -> continue
                        type.isVararg -> if (varargMatchersCount++ > 0) {
                            reporter.reportOn(arg.source, Diagnostics.SINGLE_VARARG_MATCHER_ALLOWED)
                        }
                        else -> reporter.reportOn(arg.source, Diagnostics.ILLEGAL_SPREAD_FOR_VARARG)
                    }
                }
                matchersProcessor.usesVarargMatchers(arg) -> {
                    reporter.reportOn(arg.source, Diagnostics.VARARG_MATCHER_USED_WITHOUT_SPREAD)
                }
            }
        }
    }

    object Diagnostics {
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
        val VARIABLE_OUT_OF_SCOPE by error1<KtElement, FirVariableSymbol<*>>()
        val VARIABLE_NOT_A_MATCHER by error1<KtElement, FirVariableSymbol<*>>()
        val INCOMPATIBLE_VARIABLE_TYPE by error3<KtElement, FirVariableSymbol<*>, String, String>()
        val MATCHER_PASSED_TO_METHOD_IN_MATCHER_BUILDER by error0<KtElement>()
        val MATCHER_PASSED_TO_NON_MEMBER_FUNCTION by error0<KtElement>()
        val ILLEGAL_SPREAD_FOR_VARARG by error0<KtElement>()
        val SINGLE_VARARG_MATCHER_ALLOWED by error0<KtElement>()
        val VARARG_MATCHER_WITHOUT_VARARG by error0<KtElement>()
        val VARARG_MATCHER_USED_WITHOUT_SPREAD by error0<KtElement>()
    }
}
