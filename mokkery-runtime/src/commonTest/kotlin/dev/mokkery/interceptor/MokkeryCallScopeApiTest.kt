package dev.mokkery.interceptor

import dev.mokkery.internal.context.AssociatedFunctions
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.TestMokkeryScopeLookup
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeFunctionCall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class MokkeryCallScopeApiTest {

    private val associatedFunctions = AssociatedFunctions(mapOf(Unit::class to { }), { })
    private val testTools = MokkeryTools(instanceLookup = TestMokkeryScopeLookup())
    private val mock = TestMokkeryInstanceScope()
    private val funCall = fakeFunctionCall(returnType = Int::class, args = listOf(fakeCallArg(1), fakeCallArg("str")))
    private val scope = MokkeryBlockingCallScope(funCall + testTools + mock.mokkeryContext + associatedFunctions)

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
        assertSame(associatedFunctions.supers, scope.supers)
    }
}
