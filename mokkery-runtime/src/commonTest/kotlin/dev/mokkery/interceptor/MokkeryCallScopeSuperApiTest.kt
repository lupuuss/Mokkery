@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.interceptor

import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.SuperTypeMustBeSpecifiedException
import dev.mokkery.internal.context.AssociatedFunctions
import dev.mokkery.internal.interceptor.MokkeryBlockingCallScope
import dev.mokkery.internal.interceptor.MokkerySuspendCallScope
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
    private val mockScope by lazy { TestMokkeryInstanceScope(interceptedTypes = classSupertypes) }

    private val blockingScope by lazy {
        MokkeryBlockingCallScope(
            AssociatedFunctions(
                supers = mapOf(
                    Unit::class to blocking { it[0] as Int + 1 },
                    Int::class to blocking { it[0] as Int + 2 }
                ),
                spiedFunction = null
            )
                .plus(fakeFunctionCall(returnType = Int::class, args = listOf(fakeCallArg(1))))
                .plus(mockScope.mokkeryContext)
        )

    }
    private val suspendingScope by lazy {
        MokkerySuspendCallScope(
            AssociatedFunctions(
                supers = mapOf(
                    Int::class to suspending { it[0] as Int + 2 },
                    Unit::class to suspending { it[0] as Int + 1 }), spiedFunction = null
            )
                .plus(fakeFunctionCall(returnType = Int::class, args = listOf(fakeCallArg(1))))
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

    private inline fun suspending(noinline block: suspend (List<Any?>) -> Any) = block

    private inline fun blocking(noinline block: (List<Any?>) -> Any) = block
}
