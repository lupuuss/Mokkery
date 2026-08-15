package dev.mokkery

import dev.mokkery.internal.MokkeryBlockingCallScope
import dev.mokkery.test.TestCallDispatchers
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeFunctionCall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class MokkeryCallScopeApiTest {

    private val mock = TestMokkeryInstanceScope(
        context = TestCallDispatchers(supers = mapOf(Unit::class to { args: List<Any?> -> args[0] }))
    )
    private val funCall = fakeFunctionCall(returnType = Int::class, args = listOf(fakeCallArg(1), fakeCallArg("str")))
    private val scope = MokkeryBlockingCallScope(funCall + mock.mokkeryContext)

    @Test
    fun testCallReturnsFunctionCallFromContext() {
        assertEquals(funCall, scope.call)
    }

    @Test
    fun testSelfReturnsResolvedInstance() {
        assertSame(mock, scope.self)
    }

    @Test
    fun testSelfReturnsResolvedInstanceWithCast() {
        assertSame(mock, scope.self<TestMokkeryInstanceScope>())
    }

    @Test
    fun testReturnsSupersFromContext() {
        assertEquals(listOf(Unit::class), scope.supers.keys.toList())
    }
}
