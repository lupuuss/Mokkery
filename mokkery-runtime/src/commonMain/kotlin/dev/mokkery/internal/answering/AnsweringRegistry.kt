package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryScope
import dev.mokkery.answering.Answer
import dev.mokkery.answering.SuperCall
import dev.mokkery.call
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.toCallTrace
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.names.shortToString
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.wrapInMokkeryCollection
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.self
import dev.mokkery.supers
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

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

    private val lock = reentrantLock()
    private val _answers = linkedMapOf<CallTemplate, Answer<*>>()

    override val answers: Map<CallTemplate, Answer<*>> get() = lock.withLock {
        _answers.toMutableMap()
    }

    override fun setup(template: CallTemplate, answer: Answer<*>) {
        lock.withLock {
            _answers += template to answer
        }
    }

    override fun reset() {
        lock.withLock {
            _answers.clear()
        }
    }

    override fun resolveAnswer(scope: MokkeryCallScope): Answer<*> {
        val trace = scope.toCallTrace(0)
        val collection = scope.self
            .requireInstanceScope()
            .wrapInMokkeryCollection()
        val callMatcher = scope.tools.callMatcherFactory.create(collection)
        return lock.withLock {
            _answers
                .keys
                .reversed()
                .find { callMatcher.match(trace, it).isMatching }
                ?.also { it.applyCapture(trace) }
                ?.let { _answers.getValue(it) }
        } ?: handleMissingAnswer(trace, scope)
    }

    private fun handleMissingAnswer(trace: CallTrace, scope: MokkeryCallScope): Answer<*> {
        return when (val spec = scope.instanceSpec) {
            is MokkerySpySpec -> SpiedCallAnswer
            is MokkeryMockSpec -> when (spec.mode) {
                MockMode.autofill -> Answer.Autofill
                MockMode.original if scope.supers.isNotEmpty() -> SuperCallAnswer(SuperCall.original)
                MockMode.autoUnit if scope.call.function.returnType == Unit::class -> Answer.Const(Unit)
                else -> throw CallNotMockedException(scope.tools.callTraceReceiverShortener.shortToString(trace))
            }
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

    override fun toString(): String = "AnsweringRegistry@${hashCode()}"
}
