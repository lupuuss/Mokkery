package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.CallContext
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.matches
import kotlinx.atomicfu.atomic
import kotlin.reflect.KClass

internal interface AnsweringInterceptor : MokkeryInterceptor {

    fun setup(template: CallTemplate, answer: Answer<*>)

    fun reset()
}

internal fun AnsweringInterceptor(mockMode: MockMode): AnsweringInterceptor {
    return AnsweringInterceptorImpl(mockMode)
}

private class AnsweringInterceptorImpl(
    private val mockMode: MockMode,
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
        val trace = CallTrace(context.thisRef.id, context.name, context.args, 0)
        val answers = this.answers
        return answers
            .keys
            .findLast { trace matches it }
            ?.let { answers.getValue(it) }
            ?: handleMissingAnswer(trace, context.returnType)
    }

    private fun handleMissingAnswer(trace: CallTrace, returnType: KClass<*>): Answer<*> = when {
        mockMode == MockMode.autofill -> Answer.Autofill
        mockMode == MockMode.autoUnit && returnType == Unit::class -> Answer.Const(Unit)
        else -> throw CallNotMockedException(trace.toString())
    }

    private fun CallContext.toFunctionScope() = FunctionScope(returnType, args.map(CallArg::value))
}
