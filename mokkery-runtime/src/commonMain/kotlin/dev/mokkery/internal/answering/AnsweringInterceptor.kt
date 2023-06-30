package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
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

internal fun AnsweringInterceptor(receiver: String, mockMode: MockMode): AnsweringInterceptor {
    return AnsweringInterceptorImpl(receiver, mockMode)
}

private class AnsweringInterceptorImpl(
    private val receiver: String,
    private val mockMode: MockMode,
) : AnsweringInterceptor {

    private var isSetup by atomic(false)
    private var answers by atomic(mapOf<CallTemplate, Answer<*>>())

    override fun setup(template: CallTemplate, answer: Answer<*>) {
        isSetup = true
        answers += template to answer
        isSetup = false
    }

    override fun reset() {
        answers = emptyMap()
    }

    override fun interceptCall(name: String, returnType: KClass<*>, vararg args: CallArg): Any? {
        if (isSetup) throw ConcurrentTemplatingException()
        val argsList = args.toList()
        return find(name, returnType, argsList).call(returnType, argsList.map { it.value })
    }

    override suspend fun interceptSuspendCall(name: String, returnType: KClass<*>, vararg args: CallArg): Any? {
        if (isSetup) throw ConcurrentTemplatingException()
        val argsList = args.toList()
        return find(name, returnType, argsList).callSuspend(returnType, argsList.map { it.value })
    }

    private fun find(signature: String, returnType: KClass<*>, args: List<CallArg>): Answer<*> {
        val trace = CallTrace(receiver, signature, args, 0)
        val answers = this.answers
        return answers
            .keys
            .find { trace matches it }
            ?.let { answers.getValue(it) }
            ?: handleMissingAnswer(trace, returnType)
    }

    private fun handleMissingAnswer(trace: CallTrace, returnType: KClass<*>): Answer<*> = when {
        mockMode == MockMode.autofill -> Answer.Autofill
        mockMode == MockMode.autoUnit && returnType == Unit::class -> Answer.Const(Unit)
        else -> throw CallNotMockedException(trace.toString())
    }
}
