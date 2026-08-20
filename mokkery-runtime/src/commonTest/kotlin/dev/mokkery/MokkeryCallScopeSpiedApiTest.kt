package dev.mokkery

import dev.mokkery.internal.IncorrectArgsForSpiedMethodException
import dev.mokkery.internal.MokkeryBlockingCallScope
import dev.mokkery.internal.MokkerySuspendCallScope
import dev.mokkery.internal.ObjectIsNotSpyException
import dev.mokkery.test.TestInstanceContracts
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeFunctionCall
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MokkeryCallScopeSpiedApiTest {

    private val dispatchers = TestInstanceContracts(
        spied = { args -> (args[0] as Int) + 3 },
        suspendSpied = { args -> (args[0] as Int) + 4 }
    )

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

    private fun createBlockingScope(spiedObject: Any? = Unit): MokkeryBlockingCallScope =
        MokkeryBlockingCallScope(spiedCallContext(spiedObject))

    private fun createSuspendScope(spiedObject: Any? = Unit): MokkerySuspendCallScope =
        MokkerySuspendCallScope(spiedCallContext(spiedObject))

    private fun spiedCallContext(spiedObject: Any?) = fakeFunctionCall(
        returnType = Int::class,
        args = listOf(fakeCallArg(1), fakeCallArg("str"))
    ) + TestMokkeryInstanceScope(
        spiedObject = spiedObject,
        mode = if (spiedObject == null) MockMode.strict else null,
        context = dispatchers
    ).mokkeryContext
}
