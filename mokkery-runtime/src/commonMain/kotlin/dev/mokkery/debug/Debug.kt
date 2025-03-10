package dev.mokkery.debug

import dev.mokkery.answering.Answer
import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.context.currentMockContext
import dev.mokkery.internal.context.resolveScopeOrNull
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.interceptor.AnsweringInterceptor
import dev.mokkery.internal.interceptor.CallTracingInterceptor
import dev.mokkery.internal.interceptor.MokkeryKind
import dev.mokkery.internal.mockId
import dev.mokkery.internal.mokkeryMockInterceptor

/**
 * Returns json-like structure of [obj] details (tracked calls, configured answers etc.).
 */
public fun mokkeryDebugString(obj: Any): String {
    return when (val scope = GlobalMokkeryScope.tools.resolveScopeOrNull(obj)) {
        null -> "Not a mock/spy -> $obj"
        else -> when (scope.currentMockContext.kind) {
            MokkeryKind.Spy -> mokkeryDebugSpy(scope)
            MokkeryKind.Mock ->  mokkeryDebugMock(scope)
        }
    }
}

/**
 * Prints [mokkeryDebugString] result for [obj].
 */
public fun printMokkeryDebug(obj: Any) {
    println(mokkeryDebugString(obj))
}

private fun mokkeryDebugMock(instance: MokkeryInstanceScope): String {
    return buildHierarchicalString {
        section("mock") {
            value("id", instance.mockId)
            value("mode", instance.currentMockContext.mode.name)
            answersSection(instance.mokkeryMockInterceptor.answering)
            callsSection(instance.mokkeryMockInterceptor.callTracing)
        }
    }
}

private fun mokkeryDebugSpy(instance: MokkeryInstanceScope): String {
    return buildHierarchicalString {
        section("spy") {
            value("id", instance.mockId)
            answersSection(instance.mokkeryMockInterceptor.answering)
            callsSection(instance.mokkeryMockInterceptor.callTracing)
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
