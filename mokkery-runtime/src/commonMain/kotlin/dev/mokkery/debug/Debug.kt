package dev.mokkery.debug

import dev.mokkery.answering.Answer
import dev.mokkery.internal.MokkeryKind
import dev.mokkery.internal.MokkeryMockScope
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.dynamic.MokkeryScopeLookup
import dev.mokkery.internal.tracing.CallTracingInterceptor

/**
 * Returns json-like structure of [obj] details (tracked calls, configured answers etc.).
 */
public fun mokkeryDebugString(obj: Any): String {
    return when (val scope = MokkeryScopeLookup.current.resolve(obj)) {
        is MokkeryMockScope -> when (scope.interceptor.kind) {
            MokkeryKind.Spy -> mokkeryDebugSpy(scope)
            MokkeryKind.Mock ->  mokkeryDebugMock(scope)
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

private fun mokkeryDebugMock(scope: MokkeryMockScope): String {
    return buildHierarchicalString {
        section("mock") {
            value("id", scope.id)
            value("mode", scope.interceptor.mode.name)
            answersSection(scope.interceptor.answering)
            callsSection(scope.interceptor.callTracing)
        }
    }
}

private fun mokkeryDebugSpy(scope: MokkeryMockScope): String {
    return buildHierarchicalString {
        section("spy") {
            value("id", scope.id)
            answersSection(scope.interceptor.answering)
            callsSection(scope.interceptor.callTracing)
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