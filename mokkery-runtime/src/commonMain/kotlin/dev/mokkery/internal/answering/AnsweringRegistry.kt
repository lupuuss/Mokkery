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
import dev.mokkery.internal.matcher.callMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.rendering.callTraceRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.toCallTrace
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.supers
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

internal interface AnsweringRegistry : MokkeryContext.Element {

    override val key: MokkeryContext.Key<*>
        get() = Key

    val answers: List<Pair<CallTemplate, Answer<*>>>

    fun resolveAnswer(scope: MokkeryCallScope): Answer<*>

    fun setup(template: CallTemplate, answer: Answer<*>)

    fun reset()

    companion object Key : MokkeryContext.Key<AnsweringRegistry>
}

internal val MokkeryScope.answering: AnsweringRegistry
    get() = mokkeryContext.require(AnsweringRegistry)

internal fun AnsweringRegistry(): AnsweringRegistry = AnsweringRegistryImpl()

private class AnsweringRegistryImpl : AnsweringRegistry {

    private val _answers = atomic(emptyList<Pair<CallTemplate, Answer<*>>>())

    override val answers: List<Pair<CallTemplate, Answer<*>>>
        get() = _answers.value

    override fun setup(template: CallTemplate, answer: Answer<*>) {
        _answers.update { old ->
            buildList(old.size + 1) {
                add(template to answer)
                old.forEach { if (it.first != template) add(it) }
            }
        }
    }

    override fun reset() {
        _answers.value = emptyList()
    }

    override fun resolveAnswer(scope: MokkeryCallScope): Answer<*> {
        val trace = scope.toCallTrace(0)
        val answers = _answers.value
        val callMatcher = scope.callMatcher
        val result = answers
            .find { (template) -> callMatcher.match(trace, template).isMatching }
        result?.first?.applyCapture(trace)
        return result?.second ?: handleMissingAnswer(scope, trace)
    }

    private fun handleMissingAnswer(
        scope: MokkeryCallScope,
        trace: CallTrace
    ): Answer<*> = when (val spec = scope.instanceSpec) {
        is MokkerySpySpec -> SpiedCallAnswer
        is MokkeryMockSpec -> when (spec.mode) {
            MockMode.autofill -> Answer.Autofill
            MockMode.original if scope.supers.isNotEmpty() -> SuperCallAnswer(SuperCall.original)
            MockMode.autoUnit if scope.call.function.returnType == Unit::class -> Answer.Const(Unit)
            else -> scope.withRenderingScope(instances = spec.collection) {
                throw CallNotMockedException(name = callTraceRenderer.render(trace))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun CallTemplate.applyCapture(trace: CallTrace) {
        matchers.forEach { (name, matcher) ->
            if (matcher !is Capture<*>) return@forEach
            val capture = matcher as Capture<Any?>
            // a captured null is a legitimate value, so an argument that is not there at all
            // must be skipped instead of being captured as null
            val arg = trace.args.find { it.parameter.name == name } ?: return@forEach
            capture.capture(arg.value)
        }
    }

    override fun toString(): String = "AnsweringRegistry(answers=$answers)"
}
