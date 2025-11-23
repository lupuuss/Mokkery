package dev.mokkery.internal.tracing

import dev.mokkery.context.CallArgument
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.internal.createBlockingCallScope
import dev.mokkery.internal.instanceId
import dev.mokkery.test.TestCounter
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class CallTracingRegistryTest {

    private val counter = TestCounter(0)
    private val tools = MokkeryTools(callsCounter = counter)
    private val instance1 = TestMokkeryInstanceScope(sequence = 1, context = tools + CallTracingRegistry())
    private val instance2 = TestMokkeryInstanceScope(sequence = 2, context = tools + CallTracingRegistry())

    @Test
    fun testTraceSavesCallsProperly() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        val expected = listOf(
            instance1.callTrace("call1", 0, fakeCallArg(1)),
            instance1.callTrace("call2", 1, fakeCallArg(2)),
        )
        assertEquals(expected, instance1.callTracing.all)
        assertEquals(expected, instance1.callTracing.withSession { unverified })
    }

    @Test
    fun testResetClearsCalls() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        instance1.callTracing.withSession { resetAll() }
        assertEquals(emptyList(), instance1.callTracing.all)
        assertEquals(emptyList(), instance1.callTracing.withSession { unverified })
    }

    @Test
    fun testMarkVerifiedRemovesCallFromUnverifiedInSession() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        val expected = listOf(instance1.callTrace("call2", 1, fakeCallArg(2)),)
        instance1.callTracing.withSession {
            markVerified(unverified.first())
            assertEquals(expected, unverified)
        }
    }

    @Test
    fun testMarkVerifiedRemovesCallOutsideOfSession() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        val expected = listOf(
            instance1.callTrace("call1", 0, fakeCallArg(1)),
            instance1.callTrace("call2", 1, fakeCallArg(2)),
        )
        instance1.callTracing.withSession { markVerified(unverified.first()) }
        assertEquals(expected, instance1.callTracing.all)
    }

    @Test
    fun testMarkVerifiedDoesNotRemoveFromAll() {
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance1
            .callTracing
            .trace(instance1.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        val expected = listOf(instance1.callTrace("call2", 1, fakeCallArg(2)),)
        instance1.callTracing.withSession { markVerified(unverified.first()) }
        assertEquals(expected, instance1.callTracing.withSession { unverified })
    }

    @Test
    fun testAllowsTracingWhenSessionIsStarted() {
        instance1.callTracing.withSession {
            instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
            assertEquals(1, instance1.callTracing.all.size)
        }
    }

    @Test
    fun testTracingDoesNotAffectSessionState() {
        instance1.callTracing.withSession {
            instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
            assertEquals(emptyList(), unverified)
        }
    }

    @Test
    fun testTracingEffectShouldBeVisibleInNextSession() {
        instance1.callTracing.withSession {
            instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        }
        instance1.callTracing.withSession { assertEquals(1, unverified.size) }
    }

    @Test
    fun testResetAffectsOnlyTracesFrom() {
        instance1.callTracing.withSession {
            instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        }
        instance1.callTracing.withSession { assertEquals(1, unverified.size) }
    }

    @Test
    fun testCompositeSessionUnverifiedHasCorrectOrder() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", Int::class, fakeCallArg(3)))
        val collection = MokkeryCollection(instance1, instance2)
        val expected = listOf(
            instance1.callTrace("call1", 0, fakeCallArg(1)),
            instance2.callTrace("call2", 1, fakeCallArg(2)),
            instance1.callTrace("call3", 2, fakeCallArg(3)),
        )
        assertEquals(expected, collection.withTracingSession { unverified })
    }

    @Test
    fun testCompositeSessionMarkVerifiedAffectsUnverifiedInSession() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", Int::class, fakeCallArg(3)))
        instance2.callTracing.trace(instance2.blockingCallScope("call4", Int::class, fakeCallArg(4)))
        val collection = MokkeryCollection(instance1, instance2)
        val calls = listOf(
            instance1.callTrace("call1", 0, fakeCallArg(1)),
            instance2.callTrace("call2", 1, fakeCallArg(2)),
            instance1.callTrace("call3", 2, fakeCallArg(3)),
            instance2.callTrace("call4", 3, fakeCallArg(4)),
        )
        collection.withTracingSession {
            markVerified(calls[1])
            markVerified(calls[2])
            assertEquals(listOf(calls[0], calls[3]), unverified)
        }
    }

    @Test
    fun testCompositeSessionMarkVerifiedResultsInCorrectUnverifiedForEachInstance() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", Int::class, fakeCallArg(3)))
        instance2.callTracing.trace(instance2.blockingCallScope("call4", Int::class, fakeCallArg(4)))
        val collection = MokkeryCollection(instance1, instance2)
        val calls = listOf(
            instance1.callTrace("call1", 0, fakeCallArg(1)),
            instance2.callTrace("call2", 1, fakeCallArg(2)),
            instance1.callTrace("call3", 2, fakeCallArg(3)),
            instance2.callTrace("call4", 3, fakeCallArg(4)),
        )
        collection.withTracingSession {
            markVerified(calls[1])
            markVerified(calls[2])
        }
        assertEquals(listOf(calls[0]), instance1.callTracing.withSession { unverified })
        assertEquals(listOf(calls[3]), instance2.callTracing.withSession { unverified })
    }

    @Test
    fun testCompositeSessionResetsUnverifiedInSession() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", Int::class, fakeCallArg(3)))
        instance2.callTracing.trace(instance2.blockingCallScope("call4", Int::class, fakeCallArg(4)))
        val collection = MokkeryCollection(instance1, instance2)
        val calls = listOf(
            instance1.callTrace("call1", 0, fakeCallArg(1)),
            instance2.callTrace("call2", 1, fakeCallArg(2)),
            instance1.callTrace("call3", 2, fakeCallArg(3)),
            instance2.callTrace("call4", 3, fakeCallArg(4)),
        )
        collection.withTracingSession {
            markVerified(calls[1])
            markVerified(calls[2])
            resetAll()
            assertEquals(emptyList(), unverified)
        }
    }

    @Test
    fun testCompositeSessionResetsOutsideOfSession() {
        instance1.callTracing.trace(instance1.blockingCallScope("call1", Int::class, fakeCallArg(1)))
        instance2.callTracing.trace(instance2.blockingCallScope("call2", Int::class, fakeCallArg(2)))
        instance1.callTracing.trace(instance1.blockingCallScope("call3", Int::class, fakeCallArg(3)))
        instance2.callTracing.trace(instance2.blockingCallScope("call4", Int::class, fakeCallArg(4)))
        val collection = MokkeryCollection(instance1, instance2)
        val calls = listOf(
            instance1.callTrace("call1", 0, fakeCallArg(1)),
            instance2.callTrace("call2", 1, fakeCallArg(2)),
            instance1.callTrace("call3", 2, fakeCallArg(3)),
            instance2.callTrace("call4", 3, fakeCallArg(4)),
        )
        collection.withTracingSession {
            markVerified(calls[1])
            markVerified(calls[2])
            resetAll()
        }
        assertEquals(emptyList(), instance1.callTracing.all)
        assertEquals(emptyList(), instance1.callTracing.withSession { unverified })
        assertEquals(emptyList(), instance2.callTracing.all)
        assertEquals(emptyList(), instance2.callTracing.withSession { unverified })
    }

    private fun TestMokkeryInstanceScope.callTrace(
        name: String,
        orderStamp: Long,
        vararg args: CallArgument,
    ) = CallTrace(
        instanceId = instanceId,
        name = name,
        args = args.asList(),
        orderStamp = orderStamp
    )

    private fun TestMokkeryInstanceScope.blockingCallScope(
        name: String,
        returnType: KClass<*>,
        vararg args: CallArgument
    ) = createBlockingCallScope(
        name = name,
        returnType = returnType,
        args = args.asList()
    )
}
