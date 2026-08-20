package dev.mokkery

import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.SuperTypeMustBeSpecifiedException
import dev.mokkery.internal.MokkeryBlockingCallScope
import dev.mokkery.internal.MokkerySuspendCallScope
import dev.mokkery.test.TestInstanceContracts
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeFunctionCall
import dev.mokkery.test.runTest
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MokkeryCallScopeSuperApiTest {

    private var classSupertypes: List<KClass<*>> = listOf(Unit::class)

    private val dispatchers = TestInstanceContracts(
        supers = mapOf(
            Unit::class to { args: List<Any?> -> args[0] as Int + 1 },
            Int::class to { args: List<Any?> -> args[0] as Int + 2 },
        ),
        suspendSupers = mapOf(
            Int::class to { args: List<Any?> -> args[0] as Int + 2 },
            Unit::class to { args: List<Any?> -> args[0] as Int + 1 },
        )
    )

    private val mockScope by lazy {
        TestMokkeryInstanceScope(interceptedTypes = classSupertypes, context = dispatchers)
    }

    private val blockingScope by lazy {
        MokkeryBlockingCallScope(
            fakeFunctionCall(returnType = Int::class, args = listOf(fakeCallArg(1)))
                .plus(mockScope.mokkeryContext)
        )
    }

    private val suspendingScope by lazy {
        MokkerySuspendCallScope(
            fakeFunctionCall(returnType = Int::class, args = listOf(fakeCallArg(1)))
                .plus(mockScope.mokkeryContext)
        )
    }

    @Test
    fun testCallSuper() {
        assertEquals(2, blockingScope.callSuper(Unit::class, listOf(1)))
    }

    @Test
    fun testCallSuspendSuper() = runTest {
        assertEquals(3, suspendingScope.callSuper(Int::class, listOf(1)))
    }

    @Test
    fun testCallOriginal() {
        assertEquals(2, blockingScope.callOriginal(listOf(1)))
    }

    @Test
    fun testCallSuspendOriginal() = runTest {
        classSupertypes = listOf(Int::class)
        assertEquals(3, suspendingScope.callOriginal(listOf(1)))
    }


    @Test
    fun testCallOriginalWhenMultipleSuperTypes() {
        classSupertypes = listOf(Unit::class, Float::class)
        assertEquals(2, blockingScope.callOriginal(listOf(1)))
    }

    @Test
    fun testCallSuspendOriginalWhenMultipleSuperTypes() = runTest {
        classSupertypes = listOf(Int::class, Float::class)
        assertEquals(3, suspendingScope.callOriginal(listOf(1)))
    }

    @Test
    fun testCallOriginalFailsWhenNoSuperCallForInterceptedSupertype() {
        classSupertypes = listOf(String::class)
        assertFailsWith<MissingSuperMethodException> {
            blockingScope.callOriginal(listOf(1))
        }
    }

    @Test
    fun testCallSuspendFailsWhenNoSuperCallForInterceptedSupertype() = runTest {
        classSupertypes = listOf(String::class)
        assertFailsWith<MissingSuperMethodException> {
            suspendingScope.callOriginal(listOf(1))
        }
    }

    @Test
    fun testCallOriginalFailsWhenMultipleMatchingSuperCallsForInterceptedTypes() {
        classSupertypes = listOf(Int::class, Unit::class)
        assertFailsWith<SuperTypeMustBeSpecifiedException> {
            blockingScope.callOriginal(listOf(1))
        }
    }

    @Test
    fun testCallSuspendFailsWhenMultipleMatchingSuperCallsForInterceptedTypes() = runTest {
        classSupertypes = listOf(Int::class, Unit::class)
        assertFailsWith<SuperTypeMustBeSpecifiedException> {
            suspendingScope.callOriginal(listOf(1))
        }
    }
}
