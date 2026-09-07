package dev.mokkery.test

import dev.mokkery.internal.tracing.CallTrace
import kotlin.test.assertEquals

internal fun assertTracesEqual(expected: List<CallTrace>, actual: List<CallTrace>) {
    assertEquals(expected.map(CallTrace::toString), actual.map(CallTrace::toString))
}
