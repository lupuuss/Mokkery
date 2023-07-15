package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.CallContext
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.internal.tracing.CallTrace
import kotlinx.atomicfu.atomic
import kotlin.reflect.KClass

internal interface AnsweringInterceptor : MokkeryInterceptor {

    fun setup(template: CallTemplate, answer: Answer<*>)

    fun reset()
}

internal fun AnsweringInterceptor(mockMode: MockMode, callMatcher: CallMatcher = CallMatcher()): AnsweringInterceptor {
    return AnsweringInterceptorImpl(mockMode, callMatcher)
}

private class AnsweringInterceptorImpl(
    private val mockMode: MockMode,
    private val callMatcher: CallMatcher,
) : AnsweringInterceptor {

    private var isSetup by atomic(false)
    private var answers by atomic(linkedMapOf<CallTemplate, Answer<*>>())

    override fun setup(template: CallTemplate, answer: Answer<*>) {
        isSetup = true
        answers += template to answer
        isSetup = false
    }

    override fun reset() {
        answers = linkedMapOf()
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
        val trace = CallTrace(context.self.id, context.name, context.args, 0)
        val answers = this.answers
        return answers
            .keys
            .findLast { callMatcher.matches(trace, it) }
            ?.let { answers.getValue(it) }
            ?: handleMissingAnswer(trace, context.returnType)
    }

    private fun handleMissingAnswer(trace: CallTrace, returnType: KClass<*>): Answer<*> = when {
        mockMode == MockMode.autofill -> Answer.Autofill
        mockMode == MockMode.autoUnit && returnType == Unit::class -> Answer.Const(Unit)
        else -> throw CallNotMockedException(trace.toString())
    }

    private fun CallContext.toFunctionScope() = FunctionScope(returnType, args.map(CallArg::value), self)
}
