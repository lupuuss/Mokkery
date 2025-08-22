package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.MokkeryScope
import dev.mokkery.answering.Answer
import dev.mokkery.answering.SuperCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.supers
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.calls.isMatching
import dev.mokkery.internal.context.associatedFunctions
import dev.mokkery.internal.context.mockSpec
import dev.mokkery.internal.context.toCallTrace
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.isSpy
import dev.mokkery.internal.names.shortToString
import dev.mokkery.matcher.capture.Capture
import kotlinx.atomicfu.atomic

internal interface AnsweringRegistry : MokkeryContext.Element {

    override val key: MokkeryContext.Key<*>
        get() = Key

    val answers: Map<CallTemplate, Answer<*>>

    fun resolveAnswer(scope: MokkeryCallScope): Answer<*>

    fun setup(template: CallTemplate, answer: Answer<*>)

    fun reset()

    companion object Key : MokkeryContext.Key<AnsweringRegistry>
}

internal val MokkeryScope.answering: AnsweringRegistry
    get() = mokkeryContext.require(AnsweringRegistry)

internal fun AnsweringRegistry(): AnsweringRegistry = AnsweringRegistryImpl()

private class AnsweringRegistryImpl : AnsweringRegistry {

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

    override fun resolveAnswer(scope: MokkeryCallScope): Answer<*> {
        if (modifiers.value > 0) throw ConcurrentTemplatingException()
        val trace = scope.toCallTrace(0)
        val answers = this._answers
        val callMatcher = scope.tools.callMatcher
        return answers
            .keys
            .reversed()
            .find { callMatcher.match(trace, it).isMatching }
            ?.also { it.applyCapture(trace) }
            ?.let { answers.getValue(it) }
            ?: handleMissingAnswer(trace, scope)
    }

    private fun handleMissingAnswer(trace: CallTrace, scope: MokkeryCallScope): Answer<*> {
        val mockMode = scope.mockSpec.mode
        return when {
            scope.isSpy -> SpiedCallAnswer
            mockMode == MockMode.autofill -> Answer.Autofill
            mockMode == MockMode.original && scope.supers.isNotEmpty() -> SuperCallAnswer<Any?>(SuperCall.original)
            mockMode == MockMode.autoUnit && scope.call.function.returnType == Unit::class -> Answer.Const(Unit)
            else -> throw CallNotMockedException(scope.tools.callTraceReceiverShortener.shortToString(trace))
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

    private inline fun modify(block: () -> Unit) {
        if (modifiers.getAndIncrement() > 0) throw ConcurrentTemplatingException()
        block()
        modifiers.decrementAndGet()
    }

    override fun toString(): String = "AnsweringRegistry@${hashCode()}"
}
