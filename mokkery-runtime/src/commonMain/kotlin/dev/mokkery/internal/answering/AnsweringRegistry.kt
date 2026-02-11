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
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.names.withShorterNames
import dev.mokkery.internal.render.callTrace
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.toCallTrace
import dev.mokkery.internal.toMokkeryCollection
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.self
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
        val collection = scope.self
            .requireInstanceScope()
            .toMokkeryCollection()
        val callMatcher = scope.tools.callMatcherFactory.create(collection)
        val answers = _answers.value
        val result = answers
            .find { (template) -> callMatcher.match(trace, template).isMatching }
        result?.first?.applyCapture(trace)
        return result?.second ?: handleMissingAnswer(scope, collection, trace)
    }

    private fun handleMissingAnswer(
        scope: MokkeryCallScope,
        collection: MokkeryCollection,
        trace: CallTrace
    ): Answer<*> = when (val spec = scope.instanceSpec) {
        is MokkerySpySpec -> SpiedCallAnswer
        is MokkeryMockSpec -> when (spec.mode) {
            MockMode.autofill -> Answer.Autofill
            MockMode.original if scope.supers.isNotEmpty() -> SuperCallAnswer(SuperCall.original)
            MockMode.autoUnit if scope.call.function.returnType == Unit::class -> Answer.Const(Unit)
            else -> {
                val aliases = collection.withShorterNames(scope.tools.namesShortener)
                throw CallNotMockedException(
                    name = scope.tools
                        .renderers
                        .callTrace(aliases = aliases)
                        .render(trace)
                )
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

    override fun toString(): String = "AnsweringRegistry(answers=$answers)"
}
