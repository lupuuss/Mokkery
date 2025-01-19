@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.internal.interceptor

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
import dev.mokkery.answering.SuperCall
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.interceptor.call
import dev.mokkery.interceptor.toFunctionScope
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.answering.DelegateAnswer
import dev.mokkery.internal.answering.SuperCallAnswer
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.isMatching
import dev.mokkery.internal.context.associatedFunctions
import dev.mokkery.internal.context.toCallTrace
import dev.mokkery.internal.context.tools
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

    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        checkIfIsModified()
        return findAnswerFor(scope).call(scope.toFunctionScope())
    }

    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        checkIfIsModified()
        return findAnswerFor(scope).callSuspend(scope.toFunctionScope())
    }

    private fun findAnswerFor(scope: MokkeryCallScope): Answer<*> {
        val trace = scope.toCallTrace(0)
        val answers = this._answers
        val callMatcher = scope.mokkeryContext.tools.callMatcher
        return answers
            .keys
            .reversed()
            .find { callMatcher.match(trace, it).isMatching }
            ?.also { it.applyCapture(trace) }
            ?.let { answers.getValue(it) }
            ?: handleMissingAnswer(trace, scope)
    }

    private fun handleMissingAnswer(trace: CallTrace, scope: MokkeryCallScope): Answer<*> {
        val spyDelegate = scope.associatedFunctions.spiedFunction
        return when {
            spyDelegate != null -> DelegateAnswer(spyDelegate)
            mockMode == MockMode.autofill -> Answer.Autofill
            mockMode == MockMode.original && scope.associatedFunctions.supers.isNotEmpty() -> {
                SuperCallAnswer<Any?>(SuperCall.original)
            }
            mockMode == MockMode.autoUnit && scope.call.function.returnType == Unit::class -> Answer.Const(Unit)
            else -> throw CallNotMockedException(scope.mokkeryContext.tools.callTraceReceiverShortener.shortToString(trace))
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
