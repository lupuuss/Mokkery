package dev.mokkery.answering

import dev.mokkery.internal.unsafeCast
import dev.mokkery.test.TestMokkeryInterceptorScope
import dev.mokkery.test.TestMokkeryScopeLookup
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FunctionScopeTest {

    private val scope = FunctionScope(
        returnType = Int::class,
        args = listOf(1),
        self = Unit,
        supers = mapOf(
            Unit::class to { it[0] as Int + 1 },
            Int::class to suspending { it[0] as Int + 2 }
        )
    )

    @Test
    fun testCallSuper() {
        assertEquals(2, scope.callSuper(Unit::class, listOf(1)))
    }

    @Test
    fun testCallSuspendSuper() = runTest {
        assertEquals(3, scope.callSuspendSuper(Int::class, listOf(1)))
    }


    @Test
    fun testCallOriginal() {
        val lookup = TestMokkeryScopeLookup {
            TestMokkeryInterceptorScope(interceptedType = Unit::class)
        }
        assertEquals(2, scope.callOriginal(lookup, listOf(1)))
    }

    @Test
    fun testCallSuspendOriginal() = runTest {
        val lookup = TestMokkeryScopeLookup {
            TestMokkeryInterceptorScope(interceptedType = Int::class)
        }
        assertEquals(3, scope.callSuspendOriginal(lookup, listOf(1)))
    }


    @Test
    fun testEquality() {
        assertEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap()),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap())
        )
        assertNotEquals(
            FunctionScope(String::class, listOf(1, 2, 3), Unit, emptyMap()),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap())
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(2, 3), Unit, emptyMap()),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap())
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap()),
            FunctionScope(Int::class, listOf(1, 2, 3), 1, emptyMap())
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, mapOf(Unit::class to { })),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap())
        )
    }

    private fun <T> suspending(block: suspend (List<Any?>) -> Any): T = block.unsafeCast()
}
