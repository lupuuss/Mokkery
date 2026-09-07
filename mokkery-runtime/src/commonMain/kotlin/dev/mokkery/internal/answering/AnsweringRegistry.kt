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
import dev.mokkery.internal.availableSuperCallTypes
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.matcher.CallEntry
import dev.mokkery.internal.matcher.asCallEntry
import dev.mokkery.internal.matcher.callMatcher
import dev.mokkery.internal.rendering.callEntryRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.matcher.capture.Capture
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
        val entry = scope.asCallEntry()
        val answers = _answers.value
        val callMatcher = scope.callMatcher
        val result = answers
            .find { (template) -> callMatcher.areMatching(template, entry) }
        result?.first?.applyCapture(entry)
        return result?.second ?: handleMissingAnswer(scope, entry)
    }

    private fun handleMissingAnswer(
        scope: MokkeryCallScope,
        entry: CallEntry
    ): Answer<*> = when (val spec = scope.instanceSpec) {
        is MokkerySpySpec -> SpiedCallAnswer
        is MokkeryMockSpec -> when (spec.mode) {
            MockMode.autofill -> Answer.Autofill
            MockMode.original if scope.availableSuperCallTypes().isNotEmpty() -> SuperCallAnswer(SuperCall.original)
            MockMode.autoUnit if scope.call.function.returnType == Unit::class -> Answer.Const(Unit)
            else -> scope.withRenderingScope {
                throw CallNotMockedException(name = callEntryRenderer.render(entry))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun CallTemplate.applyCapture(entry: CallEntry) {
        val args = entry.args
        matchers.forEachIndexed { index, matcher ->
            if (matcher !is Capture<*>) return@forEachIndexed
            val capture = matcher as Capture<Any?>
            capture.capture(args[index])
        }
    }

    override fun toString(): String = "AnsweringRegistry(answers=$answers)"
}
