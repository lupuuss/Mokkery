@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.internal.interceptor

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
import dev.mokkery.answering.SuperCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.call
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.toFunctionScope
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.answering.DelegateAnswer
import dev.mokkery.internal.answering.SuperCallAnswer
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.isMatching
import dev.mokkery.internal.context.associatedFunctions
import dev.mokkery.internal.context.toTrace
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.mokkeryInstanceLookup
import dev.mokkery.internal.names.shortToString
import dev.mokkery.matcher.capture.Capture
import kotlinx.atomicfu.atomic

internal interface AnsweringInterceptor : MokkeryCallInterceptor {

    val answers: Map<CallTemplate, Answer<*>>

    fun setup(template: CallTemplate, answer: Answer<*>)

    fun reset()
}

internal fun AnsweringInterceptor(mockMode: MockMode): AnsweringInterceptor = AnsweringInterceptorImpl(mockMode)

private class AnsweringInterceptorImpl(private val mockMode: MockMode) : AnsweringInterceptor {

    private val modifiers = atomic(0)
    private val _answers = linkedMapOf<CallTemplate, Answer<*>>()
    override val answers: Map<CallTemplate, Answer<*>> get() = _answers.toMutableMap()

    override fun setup(template: CallTemplate, answer: Answer<*>) {
        modify {
            _answers += template to answer
        }
    }

    override fun reset() {
        modify {
            _answers.clear()
        }
    }

    override fun intercept(scope: MokkeryCallScope): Any? {
        checkIfIsModified()
        return findAnswerFor(scope.context).call(scope.toFunctionScope())
    }

    override suspend fun interceptSuspend(scope: MokkeryCallScope): Any? {
        checkIfIsModified()
        return findAnswerFor(scope.context).callSuspend(scope.toFunctionScope())
    }

    private fun findAnswerFor(context: MokkeryContext): Answer<*> {
        val trace = context.toTrace(0)
        val answers = this._answers
        val callMatcher = context.tools.callMatcher
        return answers
            .keys
            .reversed()
            .find { callMatcher.match(trace, it).isMatching }
            ?.also { it.applyCapture(trace) }
            ?.let { answers.getValue(it) }
            ?: handleMissingAnswer(trace, context)
    }

    private fun handleMissingAnswer(trace: CallTrace, context: MokkeryContext): Answer<*> {
        val functions = context.associatedFunctions
        val spyDelegate = functions.spiedFunction
        val receiverShortener = context.tools.callTraceReceiverShortener
        return when {
            spyDelegate != null -> DelegateAnswer(spyDelegate)
            mockMode == MockMode.autofill -> Answer.Autofill
            mockMode == MockMode.original && functions.supers.isNotEmpty() -> {
                SuperCallAnswer<Any?>(SuperCall.original, context.mokkeryInstanceLookup)
            }
            mockMode == MockMode.autoUnit && context.call.function.returnType == Unit::class -> Answer.Const(Unit)
            else -> throw CallNotMockedException(receiverShortener.shortToString(trace))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun CallTemplate.applyCapture(trace: CallTrace) {
        matchers.forEach { (name, matcher) ->
            if (matcher !is Capture<*>) return@forEach
            val capture = matcher as Capture<Any?>
            val argValue = trace.args.find { it.parameter.name == name }?.value
            capture.capture(argValue)
        }
    }

    private inline fun checkIfIsModified() {
        if (modifiers.value > 0) throw ConcurrentTemplatingException()
    }

    private inline fun modify(block: () -> Unit) {
        if (modifiers.getAndIncrement() > 0) throw ConcurrentTemplatingException()
        block()
        modifiers.decrementAndGet()
    }
}
