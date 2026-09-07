package dev.mokkery.debug

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.answering.Answer
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.instanceIdString
import dev.mokkery.internal.mokkeryScope
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.rendering.callEntryRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.tracing.callTracing
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable

/**
 * Returns json-like structure of [obj] details (tracked calls, configured answers etc.).
 */
public fun mokkeryDebugString(obj: Any): String {
    return when (val scope = obj.mokkeryScope) {
        null -> "Not a mock/spy -> $obj"
        else -> when (val spec = scope.instanceSpec) {
            is MokkerySpySpec -> mokkeryDebugSpy(scope)
            is MokkeryMockSpec ->  mokkeryDebugMock(scope, spec)
        }
    }
}

/**
 * Prints [mokkeryDebugString] result for [obj].
 */
public fun printMokkeryDebug(obj: Any) {
    println(mokkeryDebugString(obj))
}

private fun mokkeryDebugMock(instance: MokkeryInstanceScope, spec: MokkeryMockSpec): String {
    return buildHierarchicalString {
        section("mock") {
            value("id", instance.instanceIdString)
            value("mode", spec.mode.name)
            answersSection(instance)
            callsSection(instance)
        }
    }
}

private fun mokkeryDebugSpy(instance: MokkeryInstanceScope): String {
    return buildHierarchicalString {
        section("spy") {
            value("id", instance.instanceIdString)
            answersSection(instance)
            callsSection(instance)
        }
    }
}

private fun HierarchicalStringBuilder.callsSection(instance: MokkeryInstanceScope) {
    section("calls") {
        val calls = instance.callTracing.all
        if (calls.isEmpty()) {
            line("")
            return@section
        }
        instance.withRenderingScope(receiverRendering = false) {
            calls.forEach { line(callEntryRenderer.render(it)) }
        }
    }
}

private fun HierarchicalStringBuilder.answersSection(instance: MokkeryInstanceScope) {
    section("answers") {
        val answering = instance.answering
        if (answering.answers.isEmpty()) {
            line("")
        } else {
            instance.withRenderingScope(receiverRendering = false) {
                answering.answers.forEach { (template, answer) ->
                    line("${callTemplateRenderer.render(template)} ${answer.renderOrDescription()}")
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
context(scope: MokkeryRenderingScope)
private fun Answer<*>.renderOrDescription(): String = when (this) {
    is Renderable -> render()
    else -> description()
}
