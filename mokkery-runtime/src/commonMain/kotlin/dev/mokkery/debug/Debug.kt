package dev.mokkery.debug

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.instanceIdString
import dev.mokkery.internal.mokkeryScope
import dev.mokkery.internal.render.callTemplate
import dev.mokkery.internal.render.callTrace
import dev.mokkery.internal.tracing.callTracing

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
        val traceRenderer = instance.tools.renderers.callTrace()
        calls.forEach { line(traceRenderer.render(it)) }
    }
}

private fun HierarchicalStringBuilder.answersSection(instance: MokkeryInstanceScope) {
    section("answers") {
        val answering = instance.answering
        if (answering.answers.isEmpty()) {
            line("")
        } else {
            val templateRenderer = instance.tools.renderers.callTemplate(renderReceiver = false)
            answering.answers.forEach { (template, answer) ->
                line("${templateRenderer.render(template)} ${answer.description()}")
            }
        }
    }
}
