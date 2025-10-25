package dev.mokkery.debug

import dev.mokkery.answering.Answer
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.mokkeryScope
import dev.mokkery.internal.context.MokkeryMockSpec
import dev.mokkery.internal.context.MokkerySpySpec
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.instanceIdString
import dev.mokkery.internal.tracing.CallTracingRegistry
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
            answersSection(instance.answering)
            callsSection(instance.callTracing)
        }
    }
}

private fun mokkeryDebugSpy(instance: MokkeryInstanceScope): String {
    return buildHierarchicalString {
        section("spy") {
            value("id", instance.instanceIdString)
            answersSection(instance.answering)
            callsSection(instance.callTracing)
        }
    }
}

private fun HierarchicalStringBuilder.callsSection(callTracing: CallTracingRegistry) {
    section("calls") {
        val calls = callTracing.all
        if (calls.isEmpty()) {
            line("")
            return@section
        }
        calls.forEach { line(it.toStringNoMockId()) }
    }
}

private fun HierarchicalStringBuilder.answersSection(answering: AnsweringRegistry) {
    section("answers") {
        if (answering.answers.isEmpty()) {
            line("")
        } else {
            answering.answers.forEach { (template, answer) ->
                line("${template.toStringNoMockId()} ${answer.debug()}")
            }
        }
    }
}

private fun Answer<*>.debug(): String = description()
