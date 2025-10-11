package dev.mokkery

import dev.mokkery.internal.IncorrectArgsForSpiedMethodException
import dev.mokkery.internal.MokkeryBlockingCallScope
import dev.mokkery.internal.MokkerySuspendCallScope
import dev.mokkery.internal.ObjectIsNotSpyException
import dev.mokkery.internal.context.AssociatedFunctions
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeFunctionCall
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MokkeryCallScopeSpiedApiTest {

    @Test
    fun testCallSpiedCallsSpiedFunction() {
        assertEquals(4, createBlockingScope().callSpied(listOf(1, "str")))
    }

    @Test
    fun testCallSpiedCallsSpiedFunctionSuspend() = runTest {
        assertEquals(5, createSuspendScope().callSpied(listOf(1, "str")))
    }

    @Test
    fun testCallSpiedFailsWhenIncorrectAmountOfArgs() {
        assertFailsWith<IncorrectArgsForSpiedMethodException> {
            createBlockingScope().callSpied(listOf(1))
        }
    }

    @Test
    fun testCallSpiedFailsWhenIncorrectAmountOfArgsSuspend() = runTest {
        assertFailsWith<IncorrectArgsForSpiedMethodException> {
            createSuspendScope().callSpied(listOf(1))
        }
    }

    @Test
    fun testCallSpiedFailsWhenNotSpy() {
        assertFailsWith<ObjectIsNotSpyException> {
            createBlockingScope(spiedObject = null).callSpied(listOf(1, "str"))
        }
    }

    @Test
    fun testCallSpiedFailsWhenNotSpySuspend() = runTest {
        assertFailsWith<ObjectIsNotSpyException> {
            createSuspendScope(spiedObject = null).callSpied(listOf(1, "str"))
        }
    }

    private fun createBlockingScope(spiedObject: Any? = Unit): MokkeryBlockingCallScope {
        val spiedFunction: (List<Any?>) -> Any? = { args: List<Any?> -> (args[0] as Int) + 3 }
        val associatedFunctions = AssociatedFunctions(supers = emptyMap(), spiedFunction = spiedFunction)
        val mock = TestMokkeryInstanceScope(
            spiedObject = spiedObject,
            mode = if (spiedObject == null) MockMode.strict else null
        )
        val funCall = fakeFunctionCall(returnType = Int::class, args = listOf(fakeCallArg(1), fakeCallArg("str")))
        return MokkeryBlockingCallScope(funCall + mock.mokkeryContext + associatedFunctions)
    }

    private fun createSuspendScope(spiedObject: Any? = Unit): MokkerySuspendCallScope {
        val spiedFunction: suspend (List<Any?>) -> Any? = { args: List<Any?> -> (args[0] as Int) + 4 }
        val associatedFunctions = AssociatedFunctions(supers = emptyMap(), spiedFunction = spiedFunction)
        val mock = TestMokkeryInstanceScope(
            spiedObject = spiedObject,
            mode = if (spiedObject == null) MockMode.strict else null
        )
        val funCall = fakeFunctionCall(returnType = Int::class, args = listOf(fakeCallArg(1), fakeCallArg("str")))
        return MokkerySuspendCallScope(funCall + mock.mokkeryContext + associatedFunctions)
    }
}
