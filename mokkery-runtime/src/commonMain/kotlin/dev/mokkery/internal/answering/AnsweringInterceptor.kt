@file:Suppress("NOTHING_TO_INLINE")
package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.answering.SuperCall
import dev.mokkery.internal.CallContext
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.dynamic.MokkeryScopeLookup
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.names.CallTraceReceiverShortener
import dev.mokkery.internal.names.shortToString
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.matcher.capture.Capture
import kotlinx.atomicfu.atomic

internal interface AnsweringInterceptor : MokkeryInterceptor {

    val answers: Map<CallTemplate, Answer<*>>

    fun setup(template: CallTemplate, answer: Answer<*>)

    fun reset()
}

internal fun AnsweringInterceptor(
    mockMode: MockMode,
    callMatcher: CallMatcher = CallMatcher(),
    lookup: MokkeryScopeLookup = MokkeryScopeLookup.current,
    callTraceReceiverShortener: CallTraceReceiverShortener = CallTraceReceiverShortener,
): AnsweringInterceptor {
    return AnsweringInterceptorImpl(mockMode, callMatcher, lookup, callTraceReceiverShortener)
}

private class AnsweringInterceptorImpl(
    private val mockMode: MockMode,
    private val callMatcher: CallMatcher,
    private val lookup: MokkeryScopeLookup,
    private val callTraceReceiverShortener: CallTraceReceiverShortener,
) : AnsweringInterceptor {

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

    override fun interceptCall(context: CallContext): Any? {
        checkIfIsModified()
        return findAnswerFor(context).call(context.toFunctionScope())
    }

    override suspend fun interceptSuspendCall(context: CallContext): Any? {
        checkIfIsModified()
        return findAnswerFor(context).callSuspend(context.toFunctionScope())
    }

    private fun findAnswerFor(context: CallContext): Answer<*> {
        val trace = CallTrace(receiver = context.scope.id, name = context.name, args = context.args, orderStamp = 0)
        val answers = this._answers
        return answers
            .keys
            .reversed()
            .find { callMatcher.match(trace, it).isMatching }
            ?.also { it.applyCapture(trace) }
            ?.let { answers.getValue(it) }
            ?: handleMissingAnswer(trace, context)
    }

    private fun handleMissingAnswer(trace: CallTrace, context: CallContext): Answer<*> = when {
        mockMode == MockMode.autofill -> Answer.Autofill
        mockMode == MockMode.original && context.supers.isNotEmpty() -> SuperCallAnswer<Any?>(SuperCall.original, lookup)
        mockMode == MockMode.autoUnit && context.returnType == Unit::class -> Answer.Const(Unit)
        else -> throw CallNotMockedException(callTraceReceiverShortener.shortToString(trace))
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

    private inline fun checkIfIsModified() {
        if (modifiers.value > 0) throw ConcurrentTemplatingException()
    }

    private inline fun modify(block: () -> Unit) {
        if (modifiers.getAndIncrement() > 0) throw ConcurrentTemplatingException()
        block()
        modifiers.decrementAndGet()
    }
}
