package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery.Callable
import dev.mokkery.plugin.fir.acceptsMatcher
import dev.mokkery.plugin.fir.allNonDispatchArgumentsMapping
import dev.mokkery.plugin.fir.contextFunctionIds
import dev.mokkery.plugin.fir.dispatchReceiverClassLikeSymbolOrNull
import dev.mokkery.plugin.fir.extractArrayLiteralCall
import dev.mokkery.plugin.fir.isMokkeryMatcherScope
import dev.mokkery.plugin.fir.isSpread
import dev.mokkery.plugin.fir.nonDispatchArguments
import dev.mokkery.plugin.fir.signatureParameters
import dev.mokkery.plugin.fir.unwrapExpressionOrArgument
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirAnonymousObject
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirNamedFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.expressions.FirBooleanOperatorExpression
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirCheckedSafeCallSubject
import org.jetbrains.kotlin.fir.expressions.FirCheckNotNullCall
import org.jetbrains.kotlin.fir.expressions.FirComponentCall
import org.jetbrains.kotlin.fir.expressions.FirDoWhileLoop
import org.jetbrains.kotlin.fir.expressions.FirElvisExpression
import org.jetbrains.kotlin.fir.expressions.FirEqualityOperatorCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirImplicitInvokeCall
import org.jetbrains.kotlin.fir.expressions.FirLoop
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirStringConcatenationCall
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.expressions.FirThrowExpression
import org.jetbrains.kotlin.fir.expressions.FirTryExpression
import org.jetbrains.kotlin.fir.expressions.FirTypeOperatorCall
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.expressions.FirWhileLoop
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.calleeReference
import org.jetbrains.kotlin.fir.lastExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.references.toResolvedVariableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
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
    private val context: CheckerContext,
    private val reporter: DiagnosticReporter,
    private val configuration: CompilerConfiguration,
    private val parentFunction: FirFunction,
    private val usageContext: MatchersUsageContext,
) : FirVisitorVoid() {

    private val matchersProcessor = MatchersProcessor(
        parentFunction.symbol.signatureParameters()
            .filter { (symbol, type) -> symbol.acceptsMatcher(session) && !type.isMokkeryMatcherScope() }
            .map { it.first },
        parentFunction
    )

    private val templatingFunctions = setOf(
        Callable.every,
        Callable.everySuspend,
        Callable.verify,
        Callable.verifySuspend
    )

    private val nestedClassStack = mutableListOf<FirClassSymbol<*>>()
    private val functionsStack = mutableListOf<FirFunctionSymbol<*>>()
    private val currentCallsStack = mutableListOf<FirQualifiedAccessExpression>()
    private val scopeFunctionBlocks = mutableSetOf<FirFunctionSymbol<*>>()
    private val legalizedNonMemberFunctionWithMatchers = mutableSetOf<FirFunctionCall>()

    override fun visitComponentCall(componentCall: FirComponentCall) {
        visitFunctionCall(componentCall)
    }

    override fun visitImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall) {
        visitFunctionCall(implicitInvokeCall)
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall) {
        visitAccess(functionCall)
    }

    override fun visitPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression) {
        visitAccess(propertyAccessExpression)
    }

    override fun visitThisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression) {
        visitAccess(thisReceiverExpression)
    }

    private fun visitAccess(access: FirQualifiedAccessExpression) = context(context) {
        val symbol = access.calleeReference.toResolvedCallableSymbol()
        when {
            symbol?.callableId in contextFunctionIds -> reportSafeCallOnMatcher(access)
            matchersProcessor.isMatcher(access) -> {
                val canUseMatcher = ensureCanUseMatchersNow(access)
                if (canUseMatcher && symbol != null && symbol.isMatcherBuilder()) {
                    reportIllegalMatcherConstruction(access, symbol)
                }
            }
            symbol == null -> Unit
            symbol.callableId in templatingFunctions -> {
                reporter.reportOn(access.source, Diagnostics.ILLEGAL_NESTED_TEMPLATING, symbol.name)
            }
            symbol.isMaterializer() -> reportIllegalMatcherConstruction(access, symbol)
            access in legalizedNonMemberFunctionWithMatchers -> Unit
            matchersProcessor.isMatcher(access.dispatchReceiver) -> {
                reporter.reportOn(access.dispatchReceiver?.source, Diagnostics.ILLEGAL_MEMBER_ACCESS_ON_MATCHER)
            }
            else -> reportIllegalAccessArguments(access, symbol)
        }
        currentCallsStack.add(access)
        access.acceptChildren(this)
        currentCallsStack.removeLast()
    }

    override fun visitVariableAssignment(variableAssignment: FirVariableAssignment) = context(context) {
        val variableSymbol = variableAssignment
            .calleeReference
            ?.toResolvedVariableSymbol()
            ?: return@context variableAssignment.acceptChildren(this)
        val rValue = variableAssignment.rValue
        val lValue = variableAssignment.lValue as? FirQualifiedAccessExpression
        val source = variableAssignment.source
        when {
            lValue != null && (lValue.dispatchReceiver != null || lValue.extensionReceiver != null) -> {
                reportIllegalAccessArguments(lValue, variableSymbol, listOf(rValue))
            }
            !matchersProcessor.isMatcher(rValue) -> Unit
            else -> when (matchersProcessor.isMatcherVariable(variableSymbol)) {
                null -> reporter.reportOn(source, Diagnostics.VARIABLE_OUT_OF_SCOPE, variableSymbol)
                false -> reporter.reportOn(source, Diagnostics.VARIABLE_NOT_A_MATCHER, variableSymbol)
                true -> Unit
            }
        }
        variableAssignment.acceptChildren(this)
    }

    override fun visitProperty(property: FirProperty) {
        matchersProcessor.processVariable(property)
        reportMatchersInOperator("by", property.delegate?.unwrapExpressionOrArgument())
        property.acceptChildren(this)
    }

    override fun visitReturnExpression(returnExpression: FirReturnExpression) = context(context) {
        val result = returnExpression.result
        if (returnExpression.target.labeledElement == parentFunction && matchersProcessor.isMatchersVararg(result)) {
            reporter.reportOn(result.source, Diagnostics.VARARG_MATCHER_WITHOUT_SPREAD)
        }
        returnExpression.acceptChildren(this)
    }

    override fun visitValueParameter(valueParameter: FirValueParameter) {
        val defaultValue = valueParameter.defaultValue
        if (defaultValue != null && valueParameter.containingDeclarationSymbol == parentFunction.symbol) {
            reportIllegalMatcherArgument(valueParameter.symbol, defaultValue)
        }
        valueParameter.acceptChildren(this)
    }

    override fun visitCallableReferenceAccess(
        callableReferenceAccess: FirCallableReferenceAccess
    ) = context(context) {
        reportMatchersInOperator("::", callableReferenceAccess.explicitReceiver)
        val symbol = callableReferenceAccess.calleeReference.toResolvedCallableSymbol()
        if (symbol?.isMatcherLikeDeclaration() == true) {
            reporter.reportOn(callableReferenceAccess.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, "::")
        }
        callableReferenceAccess.acceptChildren(this)
    }

    override fun visitStringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall) = context(context) {
        stringConcatenationCall.arguments.forEachMatcher {
            reporter.reportOn(it.source, Diagnostics.ILLEGAL_MEMBER_ACCESS_ON_MATCHER)
        }
        stringConcatenationCall.acceptChildren(this)
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
        whenExpression.acceptChildren(this)
    }

    override fun visitTryExpression(tryExpression: FirTryExpression) = context(context) {
        val blocks = buildList {
            addAll(listOf(tryExpression.tryBlock))
            tryExpression.catches.mapTo(this) { it.block }
            addAll(listOfNotNull(tryExpression.finallyBlock))
        }
        blocks.forEach {
            if (matchersProcessor.isMatcher(it.lastExpression)) {
                reporter.reportOn(tryExpression.source, Diagnostics.ILLEGAL_TRY_CATCH)
            }
        }
        tryExpression.acceptChildren(this)
    }

    override fun visitWhileLoop(whileLoop: FirWhileLoop) {
        visitLoop(whileLoop)
    }

    override fun visitDoWhileLoop(doWhileLoop: FirDoWhileLoop) {
        visitLoop(doWhileLoop)
    }

    override fun visitLoop(loop: FirLoop) = context(context) {
        if (matchersProcessor.isMatcher(loop.condition)) {
            reporter.reportOn(loop.condition.source, Diagnostics.ILLEGAL_MATCHER_IN_CONDITION)
        }
        loop.acceptChildren(this)
    }

    override fun visitBooleanOperatorExpression(booleanOperatorExpression: FirBooleanOperatorExpression) {
        val operands = listOf(booleanOperatorExpression.rightOperand, booleanOperatorExpression.leftOperand)
        reportMatchersInOperator(booleanOperatorExpression.kind.token, operands)
        booleanOperatorExpression.acceptChildren(this)
    }

    override fun visitTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall) {
        reportMatchersInOperator(typeOperatorCall.operation.operator, typeOperatorCall.arguments)
        typeOperatorCall.acceptChildren(this)
    }

    override fun visitEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall) {
        reportMatchersInOperator(equalityOperatorCall.operation.operator, equalityOperatorCall.arguments)
        equalityOperatorCall.acceptChildren(this)
    }

    override fun visitElvisExpression(elvisExpression: FirElvisExpression) {
        reportMatchersInOperator("?:", elvisExpression.lhs)
        elvisExpression.acceptChildren(this)
    }

    override fun visitCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall) {
        reportMatchersInOperator("!!", checkNotNullCall.arguments)
        checkNotNullCall.acceptChildren(this)
    }

    override fun visitGetClassCall(getClassCall: FirGetClassCall) {
        reportMatchersInOperator("::class", getClassCall.argument)
        getClassCall.acceptChildren(this)
    }

    override fun visitThrowExpression(throwExpression: FirThrowExpression) {
        reportMatchersInOperator("throw", throwExpression.exception)
        throwExpression.acceptChildren(this)
    }

    override fun visitAnonymousFunction(anonymousFunction: FirAnonymousFunction) {
        functionsStack += anonymousFunction.symbol
        processScopeFunctionBlock(anonymousFunction)
        anonymousFunction.acceptChildren(this)
        scopeFunctionBlocks -= anonymousFunction.symbol
        functionsStack -= anonymousFunction.symbol
    }

    override fun visitNamedFunction(namedFunction: FirNamedFunction) {
        visitFunction(namedFunction)
    }

    override fun visitFunction(function: FirFunction) {
        functionsStack += function.symbol
        function.acceptChildren(this)
        functionsStack -= function.symbol
    }

    override fun visitClass(klass: FirClass) {
        nestedClassStack.add(klass.symbol)
        klass.acceptChildren(this)
        nestedClassStack.popLast()
    }

    override fun visitRegularClass(regularClass: FirRegularClass) {
        visitClass(regularClass)
    }

    override fun visitAnonymousObject(anonymousObject: FirAnonymousObject) {
        nestedClassStack.add(anonymousObject.symbol)
        anonymousObject.acceptChildren(this)
        nestedClassStack.popLast()
    }

    override fun visitElement(element: FirElement) {
        element.acceptChildren(this)
    }

    private fun processScopeFunctionBlock(anonymousFunction: FirAnonymousFunction) {
        val call = currentCallsStack.lastOrNull() ?: return
        if (matchersProcessor.processContextFunction(call) == anonymousFunction) {
            scopeFunctionBlocks += anonymousFunction.symbol
        }
    }

    private fun ensureCanUseMatchersNow(usage: FirExpression): Boolean = context(context) {
        if (nestedClassStack.isNotEmpty()) {
            reporter.reportOn(usage.source, Diagnostics.ILLEGAL_NESTED_CLASS_MATCHERS, nestedClassStack.last())
            return false
        }
        val nestedFunction = functionsStack
            .lastOrNull { it != parentFunction.symbol && it !in scopeFunctionBlocks }
            ?: return true
        reporter.reportOn(usage.source, Diagnostics.ILLEGAL_NESTED_FUNCTIONS_MATCHERS, nestedFunction)
        return false
    }

    private fun reportIllegalMatcherConstruction(
        access: FirQualifiedAccessExpression,
        symbol: FirCallableSymbol<*>
    ) = context(context) {
        if (matchersProcessor.isMatcher(access.dispatchReceiver)) {
            reporter.reportOn(access.dispatchReceiver?.source, Diagnostics.ILLEGAL_MEMBER_ACCESS_ON_MATCHER)
        }
        reportSafeCallOnMatcher(access)
        access
            .allNonDispatchArgumentsMapping(symbol)
            .forEach { (param, arg) -> reportIllegalMatcherArgument(param, arg) }
    }

    private fun reportSafeCallOnMatcher(access: FirQualifiedAccessExpression) = context(context) {
        val receiver = access.extensionReceiver
        if (receiver is FirCheckedSafeCallSubject && matchersProcessor.isMatcher(receiver)) {
            reporter.reportOn(receiver.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, "?.")
        }
    }

    private fun reportIllegalMatcherArgument(param: FirBasedSymbol<*>, arg: FirExpression) = context(context) {
        when {
            !param.acceptsMatcher(session) -> if (matchersProcessor.isMatcher(arg)) {
                reporter.reportOn(arg.source, Diagnostics.MATCHER_PASSED_TO_NON_MATCHER_PARAM, param)
            }
            arg is FirVarargArgumentsExpression -> arg.arguments
                .filter(matchersProcessor::isMatchersVararg)
                .forEach { reporter.reportOn(it.source, Diagnostics.VARARG_MATCHER_WITHOUT_SPREAD) }
            matchersProcessor.isMatchersVararg(arg) -> reporter.reportOn(arg.source, Diagnostics.VARARG_MATCHER_WITHOUT_SPREAD)
        }
    }

    private fun reportIllegalAccessArguments(
        access: FirQualifiedAccessExpression,
        symbol: FirCallableSymbol<*>,
        arguments: List<FirExpression> = access.nonDispatchArguments(),
    ) = context(context) {
        when {
            access.dispatchReceiver == null || symbol is FirConstructorSymbol -> {
                arguments.forEachMatcher { reportIllegalMatcherPassedToNonMember(it, symbol) }
            }
            usageContext == MatchersUsageContext.TEMPLATING && symbol.isMockable() -> {
                reportIllegalMatchersInMockableMember(access, symbol)
            }
            else -> arguments.forEachMatcher { reportIllegalMatcherPassedToMember(it, symbol) }
        }
    }

    private fun reportIllegalMatcherPassedToMember(
        value: FirExpression,
        symbol: FirCallableSymbol<*>
    ) = context(context) {
        if (usageContext == MatchersUsageContext.BUILDER) {
            reporter.reportOn(value.source, Diagnostics.MATCHER_PASSED_TO_MEMBER_IN_MATCHER_BUILDER)
            return@context
        }
        val classSymbol = symbol.dispatchReceiverClassLikeSymbolOrNull()
        when {
            symbol.isFinal -> reporter.reportOn(value.source, Diagnostics.MATCHER_USED_WITH_FINAL_MEMBER, symbol)
            classSymbol?.isFinal == true -> {
                reporter.reportOn(value.source, Diagnostics.MATCHER_USED_WITH_FINAL_CLASS, classSymbol)
            }
        }
    }

    private fun reportIllegalMatcherPassedToNonMember(
        value: FirExpression,
        symbol: FirCallableSymbol<*>
    ) = context(context) {
        reporter.reportOn(value.source, Diagnostics.ILLEGAL_MATCHER_IN_NON_MEMBER, symbol)
    }

    private fun reportIllegalMatchersInMockableMember(
        access: FirQualifiedAccessExpression,
        symbol: FirCallableSymbol<*>
    ) = context(context) {
        for ((param, arg) in access.allNonDispatchArgumentsMapping(symbol)) {
            if (param is FirValueParameterSymbol && param.isVararg) {
                reportIllegalVarargMatchers(arg as FirVarargArgumentsExpression)
            }
        }
    }

    private fun reportIllegalVarargMatchers(
        vararg: FirVarargArgumentsExpression,
        allowArrayLiteral: Boolean = true,
    ): Unit = context(context) {
        val soleArrayLiteralCall = vararg
            .arguments
            .singleOrNull()
            ?.takeIf { allowArrayLiteral && it.isSpread() }
            ?.extractArrayLiteralCall(session)
        val nestedVararg = soleArrayLiteralCall?.arguments?.firstOrNull() as? FirVarargArgumentsExpression
        if (soleArrayLiteralCall != null && nestedVararg != null) {
            legalizedNonMemberFunctionWithMatchers += soleArrayLiteralCall
            reportIllegalVarargMatchers(vararg = nestedVararg, allowArrayLiteral = false)
            return@context
        }
        var varargMatchersCount = 0
        for (arg in vararg.arguments) {
            if (!arg.isSpread()) continue
            if (matchersProcessor.isMatcher(arg) && varargMatchersCount++ > 0) {
                reporter.reportOn(arg.source, Diagnostics.SINGLE_VARARG_MATCHER_ALLOWED)
            }
        }
    }

    private fun reportMatchersInOperator(operator: String, operands: List<FirExpression?>) {
        operands.forEach { reportMatchersInOperator(operator, it) }
    }

    private fun reportMatchersInOperator(operator: String, operand: FirExpression?) = context(context) {
        if (operand != null && matchersProcessor.isMatcher(operand)) {
            reporter.reportOn(operand.source, Diagnostics.ILLEGAL_OPERATOR_USAGE, operator)
        }
    }

    private fun FirCallableSymbol<*>.isMockable(): Boolean = context(context) {
        !isFinal && dispatchReceiverClassLikeSymbolOrNull()?.isFinal != true
    }

    private inline fun <T : FirExpression?> List<T>.forEachMatcher(block: (T & Any) -> Unit) {
        forEach {
            if (it != null && matchersProcessor.isMatcher(it)) block(it)
        }
    }

    object Diagnostics : KtDiagnosticsContainer() {

        override fun getRendererFactory() = MatchersUsageDiagnosticRendererFactory()

        val MATCHER_PASSED_TO_NON_MATCHER_PARAM by error1<KtElement, FirBasedSymbol<*>>()
        val ILLEGAL_MATCHER_IN_NON_MEMBER by error1<KtElement, FirCallableSymbol<*>>()
        val ILLEGAL_MEMBER_ACCESS_ON_MATCHER by error0<KtElement>()
        val ILLEGAL_OPERATOR_USAGE by error1<KtElement, String>()
        val ILLEGAL_TRY_CATCH by error0<KtElement>()
        val ILLEGAL_NESTED_TEMPLATING by error1<KtElement, Name>()
        val ILLEGAL_NESTED_FUNCTIONS_MATCHERS by error1<KtElement, FirFunctionSymbol<*>>()
        val ILLEGAL_NESTED_CLASS_MATCHERS by error1<KtElement, FirClassSymbol<*>>()
        val ILLEGAL_MATCHER_IN_CONDITION by error0<KtElement>()
        val ILLEGAL_MATCHER_IN_WHEN_SUBJECT by error0<KtElement>()
        val VARIABLE_OUT_OF_SCOPE by error1<KtElement, FirVariableSymbol<*>>()
        val VARIABLE_NOT_A_MATCHER by error1<KtElement, FirVariableSymbol<*>>()
        val MATCHER_PASSED_TO_MEMBER_IN_MATCHER_BUILDER by error0<KtElement>()
        val SINGLE_VARARG_MATCHER_ALLOWED by error0<KtElement>()
        val MATCHER_USED_WITH_FINAL_MEMBER by error1<KtElement, FirCallableSymbol<*>>()
        val MATCHER_USED_WITH_FINAL_CLASS by error1<KtElement, FirClassLikeSymbol<*>>()
        val VARARG_MATCHER_WITHOUT_SPREAD by error0<KtElement>()
    }
}
