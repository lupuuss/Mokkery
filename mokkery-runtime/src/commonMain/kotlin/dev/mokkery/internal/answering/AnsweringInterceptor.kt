package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.CallContext
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.dynamic.MokkeryScopeLookup
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.capture.Capture
import kotlinx.atomicfu.atomic
import kotlin.reflect.KClass

internal interface AnsweringInterceptor : MokkeryInterceptor {

    val answers: Map<CallTemplate, Answer<*>>

    fun setup(template: CallTemplate, answer: Answer<*>)

    fun reset()
}

internal fun AnsweringInterceptor(
    mockMode: MockMode,
    callMatcher: CallMatcher = CallMatcher(),
    lookup: MokkeryScopeLookup = MokkeryScopeLookup.current
): AnsweringInterceptor {
    return AnsweringInterceptorImpl(mockMode, callMatcher, lookup)
}

private class AnsweringInterceptorImpl(
    private val mockMode: MockMode,
    private val callMatcher: CallMatcher,
    private val lookup: MokkeryScopeLookup,
) : AnsweringInterceptor {

    private var isSetup by atomic(false)
    private var _answers by atomic(linkedMapOf<CallTemplate, Answer<*>>())
    override val answers: Map<CallTemplate, Answer<*>> get() = _answers

    override fun setup(template: CallTemplate, answer: Answer<*>) {
        isSetup = true
        _answers += template to answer
        isSetup = false
    }

    override fun reset() {
        _answers = linkedMapOf()
    }

    override fun interceptCall(context: CallContext): Any? {
        if (isSetup) throw ConcurrentTemplatingException()
        return findAnswerFor(context).call(context.toFunctionScope())
    }

    override suspend fun interceptSuspendCall(context: CallContext): Any? {
        if (isSetup) throw ConcurrentTemplatingException()
        return findAnswerFor(context).callSuspend(context.toFunctionScope())
    }

    private fun findAnswerFor(context: CallContext): Answer<*> {
        val trace = CallTrace(receiver = context.scope.id, name = context.name, args = context.args, orderStamp = 0)
        val answers = this._answers
        return answers
            .keys
            .reversed()
            .find { callMatcher.matches(trace, it) }
            ?.also { it.applyCapture(trace) }
            ?.let { answers.getValue(it) }
            ?: handleMissingAnswer(trace, context.returnType)
    }

    private fun handleMissingAnswer(trace: CallTrace, returnType: KClass<*>): Answer<*> = when {
        mockMode == MockMode.autofill -> Answer.Autofill
        mockMode == MockMode.autoUnit && returnType == Unit::class -> Answer.Const(Unit)
        else -> throw CallNotMockedException(trace.toString())
    }

    private fun CallContext.toFunctionScope() = FunctionScope(
        returnType = returnType,
        args = args.map(CallArg::value),
        self = lookup.reverseResolve(scope),
        supers = supers
    )

    @Suppress("UNCHECKED_CAST")
    private fun CallTemplate.applyCapture(trace: CallTrace) {
        matchers.forEach { (name, matcher) ->
            if (matcher !is Capture<*>) return@forEach
            val capture = matcher as Capture<Any?>
            val argValue = trace.args.find { it.name == name }?.value
            capture.capture(argValue)
        }
    }
}
