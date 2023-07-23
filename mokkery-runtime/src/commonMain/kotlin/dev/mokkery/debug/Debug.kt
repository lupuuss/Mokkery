package dev.mokkery.debug

import dev.mokkery.answering.Answer
import dev.mokkery.internal.MokkeryMock
import dev.mokkery.internal.MokkeryMockScope
import dev.mokkery.internal.MokkerySpy
import dev.mokkery.internal.MokkerySpyScope
import dev.mokkery.internal.dynamic.MokkeryScopeLookup

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
            value("id", scope.toString())
            value("mode", mock.mode.name)
            section("answers") {
                if (mock.answering.answers.isEmpty()) {
                    line("")
                } else {
                    mock.answering.answers.forEach { (template, answer) ->
                        line("${template.toStringNoReceiver()} ${answer.debug()}")
                    }
                }
            }
            callsSection(mock)
        }
    }
}

private fun mokkeryDebugSpy(scope: MokkerySpyScope, spy: MokkerySpy): String {
    return buildHierarchicalString {
        section("spy") {
            value("id", scope.toString())
            callsSection(spy)
        }
    }
}

private fun HierarchicalStringBuilder.callsSection(spy: MokkerySpy) {
    section("calls") {
        val calls = spy.callTracing.all
        if (calls.isEmpty()) {
            line("")
            return@section
        }
        calls.forEach { line(it.toStringNoReceiver()) }
    }
}

private fun Answer<*>.debug(): String = when (this) {
    is Answer.Const<*> -> "returns $value"
    is Answer.Throws -> "throws $throwable"
    is Answer.Block, is Answer.BlockSuspend<*> -> "calls { ... }"
    is Answer.Sequential -> "sequentially { ... }"
    else -> "answers $this"
}
