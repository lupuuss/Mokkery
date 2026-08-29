package dev.mokkery.internal.tracing

import dev.mokkery.context.CallArgument
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.internal.blockingCallScope
import dev.mokkery.internal.instanceId
import dev.mokkery.test.TestCounter
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.fakeFunParam
import dev.mokkery.test.fakeFunction
import dev.mokkery.MokkeryCallScope
import dev.mokkery.context.Function
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

private class RecordingCallTracingRegistry : CallTracingRegistry {

    var closedSessions = 0
        private set

    override val all: List<CallTrace> get() = emptyList()

    override fun trace(scope: MokkeryCallScope) = Unit

    override fun acquireVerifySession() = object : CallTracingRegistry.VerifySession {
        override val unverified: List<CallTrace> get() = emptyList()
        override fun resetAll() = Unit
        override fun markVerified(trace: CallTrace) = Unit
        override fun close() {
            closedSessions++
        }
    }
}

private class FailingCallTracingRegistry : CallTracingRegistry {

    override val all: List<CallTrace> get() = emptyList()

    override fun trace(scope: MokkeryCallScope) = Unit

    override fun acquireVerifySession(): CallTracingRegistry.VerifySession = error("Failed to acquire a session!")
}

class CallTracingRegistryTest {

    private val counter = TestCounter(0)
    private val tools = MokkeryTools(callsCounter = counter)
    private val functions = List(4) { index ->
        val name = "call${index + 1}"
        fakeFunction(name = name, parameters = listOf(fakeFunParam<Int>("arg")))
    }

    private val instance1 = testInstance(instanceId = 1)
    private val instance2 = testInstance(instanceId = 2)

    private fun testInstance(instanceId: Long) = TestMokkeryInstanceScope(
        instanceId = instanceId,
        functions = functions,
        context = tools + CallTracingRegistry(),
    )

    @Test
    fun testTraceSavesCallsProperly() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", fakeCallArg(2)))
        val expected = listOf(
            instance1.callTrace(0, "call1", fakeCallArg(1)),
            instance1.callTrace(1, "call2", fakeCallArg(2)),
        )
        assertEquals(expected, instance1.callTracing.all)
        assertEquals(expected, instance1.callTracing.withVerifySession { unverified })
    }

    @Test
    fun testResetClearsCalls() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", fakeCallArg(2)))
        instance1.callTracing.withVerifySession { resetAll() }
        assertEquals(emptyList(), instance1.callTracing.all)
        assertEquals(emptyList(), instance1.callTracing.withVerifySession { unverified })
    }

    @Test
    fun testMarkVerifiedRemovesCallFromUnverifiedInSession() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", fakeCallArg(2)))
        val expected = listOf(instance1.callTrace(1, "call2", fakeCallArg(2)),)
        instance1.callTracing.withVerifySession {
            markVerified(unverified.first())
            assertEquals(expected, unverified)
        }
    }

    @Test
    fun testMarkVerifiedRemovesCallOutsideOfSession() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", fakeCallArg(2)))
        val expected = listOf(
            instance1.callTrace(0, "call1", fakeCallArg(1)),
            instance1.callTrace(1, "call2", fakeCallArg(2)),
        )
        instance1.callTracing.withVerifySession { markVerified(unverified.first()) }
        assertEquals(expected, instance1.callTracing.all)
    }

    @Test
    fun testMarkVerifiedDoesNotRemoveFromAll() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", fakeCallArg(2)))
        val expected = listOf(instance1.callTrace(1, "call2", fakeCallArg(2)),)
        instance1.callTracing.withVerifySession { markVerified(unverified.first()) }
        assertEquals(expected, instance1.callTracing.withVerifySession { unverified })
    }

    @Test
    fun testAllowsTracingWhenSessionIsStarted() {
        instance1.callTracing.withVerifySession {
            instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
            assertEquals(1, instance1.callTracing.all.size)
        }
    }

    @Test
    fun testTracingDoesNotAffectSessionState() {
        instance1.callTracing.withVerifySession {
            instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
            assertEquals(emptyList(), unverified)
        }
    }

    @Test
    fun testTracingEffectShouldBeVisibleInNextSession() {
        instance1.callTracing.withVerifySession {
            instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        }
        instance1.callTracing.withVerifySession { assertEquals(1, unverified.size) }
    }

    @Test
    fun testResetAffectsOnlyTracesFrom() {
        instance1.callTracing.withVerifySession {
            instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        }
        instance1.callTracing.withVerifySession { assertEquals(1, unverified.size) }
    }

    @Test
    fun testCompositeSessionUnverifiedHasCorrectOrder() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", fakeCallArg(3)))
        val collection = MokkeryCollection(instance1, instance2)
        val expected = listOf(
            instance1.callTrace(0, "call1", fakeCallArg(1)),
            instance2.callTrace(1, "call2", fakeCallArg(2)),
            instance1.callTrace(2, "call3", fakeCallArg(3)),
        )
        assertEquals(expected, collection.withVerifySession { unverified })
    }

    @Test
    fun testCompositeSessionMarkVerifiedAffectsUnverifiedInSession() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", fakeCallArg(3)))
        instance2.callTracing.trace(instance2.blockingCallScope("call4", fakeCallArg(4)))
        val collection = MokkeryCollection(instance1, instance2)
        val calls = listOf(
            instance1.callTrace(0, "call1", fakeCallArg(1)),
            instance2.callTrace(1, "call2", fakeCallArg(2)),
            instance1.callTrace(2, "call3", fakeCallArg(3)),
            instance2.callTrace(3, "call4", fakeCallArg(4)),
        )
        collection.withVerifySession {
            markVerified(calls[1])
            markVerified(calls[2])
            assertEquals(listOf(calls[0], calls[3]), unverified)
        }
    }

    @Test
    fun testCompositeSessionMarkVerifiedResultsInCorrectUnverifiedForEachInstance() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", fakeCallArg(3)))
        instance2.callTracing.trace(instance2.blockingCallScope("call4", fakeCallArg(4)))
        val collection = MokkeryCollection(instance1, instance2)
        val calls = listOf(
            instance1.callTrace(0, "call1", fakeCallArg(1)),
            instance2.callTrace(1, "call2", fakeCallArg(2)),
            instance1.callTrace(2, "call3", fakeCallArg(3)),
            instance2.callTrace(3, "call4", fakeCallArg(4)),
        )
        collection.withVerifySession {
            markVerified(calls[1])
            markVerified(calls[2])
        }
        assertEquals(listOf(calls[0]), instance1.callTracing.withVerifySession { unverified })
        assertEquals(listOf(calls[3]), instance2.callTracing.withVerifySession { unverified })
    }

    @Test
    fun testCompositeSessionResetsUnverifiedInSession() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", fakeCallArg(3)))
        instance2.callTracing.trace(instance2.blockingCallScope("call4", fakeCallArg(4)))
        val collection = MokkeryCollection(instance1, instance2)
        val calls = listOf(
            instance1.callTrace(0, "call1", fakeCallArg(1)),
            instance2.callTrace(1, "call2", fakeCallArg(2)),
            instance1.callTrace(2, "call3", fakeCallArg(3)),
            instance2.callTrace(3, "call4", fakeCallArg(4)),
        )
        collection.withVerifySession {
            markVerified(calls[1])
            markVerified(calls[2])
            resetAll()
            assertEquals(emptyList(), unverified)
        }
    }

    @Test
    fun testCompositeSessionResetsOutsideOfSession() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", fakeCallArg(3)))
        instance2.callTracing.trace(instance2.blockingCallScope("call4", fakeCallArg(4)))
        val collection = MokkeryCollection(instance1, instance2)
        val calls = listOf(
            instance1.callTrace(0, "call1", fakeCallArg(1)),
            instance2.callTrace(1, "call2", fakeCallArg(2)),
            instance1.callTrace(2, "call3", fakeCallArg(3)),
            instance2.callTrace(3, "call4", fakeCallArg(4)),
        )
        collection.withVerifySession {
            markVerified(calls[1])
            markVerified(calls[2])
            resetAll()
        }
        assertEquals(emptyList(), instance1.callTracing.all)
        assertEquals(emptyList(), instance1.callTracing.withVerifySession { unverified })
        assertEquals(emptyList(), instance2.callTracing.all)
        assertEquals(emptyList(), instance2.callTracing.withVerifySession { unverified })
    }

    @Test
    fun testCompositeSessionReleasesAlreadyAcquiredSessionsWhenAcquiringFails() {
        val acquired = RecordingCallTracingRegistry()
        val collection = MokkeryCollection(
            TestMokkeryInstanceScope(instanceId = 1, context = tools + acquired),
            TestMokkeryInstanceScope(instanceId = 2, context = tools + FailingCallTracingRegistry()),
        )
        assertFails { collection.acquireVerifySession() }
        assertEquals(1, acquired.closedSessions)
    }

    private fun TestMokkeryInstanceScope.callTrace(
        traceId: Long,
        name: String,
        vararg args: CallArgument,
    ) = fakeCallTrace(
        traceId = traceId,
        instanceId = instanceId.id,
        typeName = instanceId.typeName,
        name = name,
        args = args.map { it.value },
    )

    private fun TestMokkeryInstanceScope.blockingCallScope(
        name: String,
        vararg args: CallArgument
    ) = blockingCallScope(
        id = Function.Id(name.hashCode().toLong()),
        args = args.map { it.value },
    )
}
