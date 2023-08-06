package dev.mokkery.answering

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FunctionScopeTest {

    private val scope = FunctionScope(
        returnType = Int::class,
        args = listOf(1, "2", 2.0),
        self = Unit,
        supers = emptyMap()
    )

    @Test
    fun testComponentsReturnsArgs() {
        val (i: Int, s: String, d: Double) = scope
        assertEquals(1, i)
        assertEquals("2", s)
        assertEquals(2.0, d)
    }

    @Test
    fun testCallSuperCallsSuperMethod() {
        var args: List<Any?> = emptyList()
        val scope = FunctionScope(
            returnType = Int::class,
            args = listOf(1, "2", 2.0),
            self = Unit,
            supers = mapOf(
                Unit::class to {
                    args = it
                    4
                }
            )
        )
        assertEquals(4, scope.callSuper(Unit::class, listOf(1, 2, 3)))
        assertEquals(listOf(1, 2, 3), args)
    }

    @Test
    fun testCallSuperReifiedCallsSuperMethod() {
        var args: List<Any?> = emptyList()
        val scope = FunctionScope(
            returnType = Int::class,
            args = listOf(1, "2", 2.0),
            self = Unit,
            supers = mapOf(
                Unit::class to {
                    args = it
                    4
                }
            )
        )
        assertEquals(4, scope.callSuper<Unit, Int>(1, 2, 3))
        assertEquals(listOf(1, 2, 3), args)
    }
    @Test
    fun testCallSuperWithPassedArgsCallsSuperMethodWithFunctionScopeArgs() {
        var args: List<Any?> = emptyList()
        val scope = FunctionScope(
            returnType = Int::class,
            args = listOf(1, "2", 2.0),
            self = Unit,
            supers = mapOf(
                Unit::class to {
                    args = it
                    4
                }
            )
        )
        assertEquals(4, scope.callSuperWithPassedArgs<Unit, Int>())
        assertEquals(scope.args, args)
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
}
