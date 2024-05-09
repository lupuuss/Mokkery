package dev.mokkery.debug

import dev.mokkery.answering.Answer
import dev.mokkery.internal.MokkeryMock
import dev.mokkery.internal.MokkeryMockScope
import dev.mokkery.internal.MokkerySpy
import dev.mokkery.internal.MokkerySpyScope
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.answering.SuperCallAnswer
import dev.mokkery.internal.description
import dev.mokkery.internal.dynamic.MokkeryScopeLookup
import dev.mokkery.internal.tracing.CallTracingInterceptor

/**
 * Returns json-like structure of [obj] details (tracked calls, configured answers etc.).
 */
public fun mokkeryDebugString(obj: Any): String {
    return when (val scope = MokkeryScopeLookup.current.resolve(obj)) {
        is MokkeryMockScope -> mokkeryDebugMock(scope, scope.interceptor)
        is MokkerySpyScope -> mokkeryDebugSpy(scope, scope.interceptor)
        else -> "Not a mock/spy -> $obj"
    }
}

/**
 * Prints [mokkeryDebugString] result for [obj].
 */
public fun printMokkeryDebug(obj: Any) {
    println(mokkeryDebugString(obj))
}

private fun mokkeryDebugMock(scope: MokkeryMockScope, mock: MokkeryMock): String {
    return buildHierarchicalString {
        section("mock") {
            value("id", scope.id)
            value("mode", mock.mode.name)
            answersSection(mock.answering)
            callsSection(mock.callTracing)
        }
    }
}

private fun mokkeryDebugSpy(scope: MokkerySpyScope, spy: MokkerySpy): String {
    return buildHierarchicalString {
        section("spy") {
            value("id", scope.id)
            answersSection(spy.answering)
            callsSection(spy.callTracing)
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

private fun Answer<*>.debug(): String = when (this) {
    is Answer.Const<*> -> "returns ${value.description()}"
    is Answer.Throws -> "throws $throwable"
    is Answer.Block, is Answer.BlockSuspend<*> -> "calls {...}"
    is Answer.Sequential -> "sequentially {...}"
    is SuperCallAnswer<*> -> toString()
    else -> "answers $this"
}
