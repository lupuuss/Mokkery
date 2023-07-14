package dev.mokkery.answering

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FunctionScopeTest {

    private val scope = FunctionScope(
        Int::class,
        listOf(1, "2", 2.0),
        Unit
    )

    @Test
    fun testComponentsReturnsArgs() {
        val (i: Int, s: String, d: Double) = scope
        assertEquals(1, i)
        assertEquals("2", s)
        assertEquals(2.0, d)
    }

    @Test
    fun testEquality() {
        assertEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit)
        )
        assertNotEquals(
            FunctionScope(String::class, listOf(1, 2, 3), Unit),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit)
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(2, 3), Unit),
            FunctionScope(Int::class, listOf(1, 2, 3), Unit)
        )
        assertNotEquals(
            FunctionScope(Int::class, listOf(1, 2, 3), Unit),
            FunctionScope(Int::class, listOf(1, 2, 3), 1)
        )
    }
}
