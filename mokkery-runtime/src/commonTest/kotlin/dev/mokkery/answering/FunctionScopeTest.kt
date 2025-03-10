@file:Suppress("NOTHING_TO_INLINE")
package dev.mokkery.answering

import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.SuperTypeMustBeSpecifiedException
import dev.mokkery.test.runTest
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class FunctionScopeTest {

    private var classSupertypes: List<KClass<*>> = listOf(Unit::class)
    private val scope by lazy {
        FunctionScope(
            returnType = Int::class,
            args = listOf(1),
            self = Unit,
            supers = mapOf(
                Unit::class to blocking{ it[0] as Int + 1 },
                Int::class to suspending { it[0] as Int + 2 }
            ),
            classSupertypes = classSupertypes
        )
    }

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
        assertEquals(2, scope.callOriginal(listOf(1)))
    }

    @Test
    fun testCallSuspendOriginal() = runTest {
        classSupertypes = listOf(Int::class)
        assertEquals(3, scope.callSuspendOriginal(listOf(1)))
    }


    @Test
    fun testCallOriginalWhenMultipleSuperTypes() {
        classSupertypes = listOf(Unit::class, Float::class)
        assertEquals(2, scope.callOriginal(listOf(1)))
    }

    @Test
    fun testCallSuspendOriginalWhenMultipleSuperTypes() = runTest {
        classSupertypes = listOf(Int::class, Float::class)
        assertEquals(3, scope.callSuspendOriginal(listOf(1)))
    }

    @Test
    fun testCallOriginalFailsWhenNoSuperCallForInterceptedSupertype() {
        classSupertypes = listOf(String::class)
        assertFailsWith<MissingSuperMethodException> {
            scope.callOriginal(listOf(1))
        }
    }

    @Test
    fun testCallSuspendFailsWhenNoSuperCallForInterceptedSupertype() = runTest {
        classSupertypes = listOf(String::class)
        assertFailsWith<MissingSuperMethodException> {
            scope.callSuspendOriginal(listOf(1))
        }
    }

    @Test
    fun testCallOriginalFailsWhenMultipleMatchingSuperCallsForInterceptedTypes() {
        classSupertypes = listOf(Int::class, Unit::class)
        assertFailsWith<SuperTypeMustBeSpecifiedException> {
            scope.callOriginal(listOf(1))
        }
    }

    @Test
    fun testCallSuspendFailsWhenMultipleMatchingSuperCallsForInterceptedTypes() = runTest {
        classSupertypes = listOf(Int::class, Unit::class)
        assertFailsWith<SuperTypeMustBeSpecifiedException> {
            scope.callSuspendOriginal(listOf(1))
        }
    }

    @Test
    fun testEquality() {
        assertEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Unit::class)),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Unit::class))
        )
        assertNotEquals(
            FunctionScope(String::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Unit::class)),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Unit::class))
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(2, 3), Unit, emptyMap(), listOf(Unit::class)),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Unit::class))
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Unit::class)),
            FunctionScope(Int::class, listOf(1, 2, 3), 1, emptyMap(), listOf(Unit::class))
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, mapOf(Unit::class to { }), listOf(Unit::class)),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Unit::class))
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Unit::class)),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit, emptyMap(), listOf(Int::class))
        )
    }

    private inline fun suspending(noinline block: suspend (List<Any?>) -> Any) = block

    private inline fun blocking(noinline block: (List<Any?>) -> Any) = block
}
