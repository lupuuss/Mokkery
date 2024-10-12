package dev.mokkery.debug

import dev.mokkery.answering.Answer
import dev.mokkery.internal.MokkeryKind
import dev.mokkery.internal.MokkeryMockInstance
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.dynamic.MokkeryInstanceLookup
import dev.mokkery.internal.tracing.CallTracingInterceptor

/**
 * Returns json-like structure of [obj] details (tracked calls, configured answers etc.).
 */
public fun mokkeryDebugString(obj: Any): String {
    return when (val instance = MokkeryInstanceLookup.current.resolve(obj)) {
        is MokkeryMockInstance -> when (instance.interceptor.kind) {
            MokkeryKind.Spy -> mokkeryDebugSpy(instance)
            MokkeryKind.Mock ->  mokkeryDebugMock(instance)
        }
        else -> "Not a mock/spy -> $obj"
    }
}

/**
 * Prints [mokkeryDebugString] result for [obj].
 */
public fun printMokkeryDebug(obj: Any) {
    println(mokkeryDebugString(obj))
}

private fun mokkeryDebugMock(instance: MokkeryMockInstance): String {
    return buildHierarchicalString {
        section("mock") {
            value("id", instance.id)
            value("mode", instance.interceptor.mode.name)
            answersSection(instance.interceptor.answering)
            callsSection(instance.interceptor.callTracing)
        }
    }
}

private fun mokkeryDebugSpy(instance: MokkeryMockInstance): String {
    return buildHierarchicalString {
        section("spy") {
            value("id", instance.id)
            answersSection(instance.interceptor.answering)
            callsSection(instance.interceptor.callTracing)
        }
    }
}

private fun HierarchicalStringBuilder.callsSection(callTracing: CallTracingInterceptor) {
    section("calls") {
        val calls = callTracing.all
        if (calls.isEmpty()) {
            line("")
            return@section
        }
        calls.forEach { line(it.toStringNoReceiver()) }
    }
}

private fun HierarchicalStringBuilder.answersSection(answering: AnsweringInterceptor) {
    section("answers") {
        if (answering.answers.isEmpty()) {
            line("")
        } else {
            answering.answers.forEach { (template, answer) ->
                line("${template.toStringNoReceiver()} ${answer.debug()}")
            }
        }
    }
}

private fun Answer<*>.debug(): String = description()
