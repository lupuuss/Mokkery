package dev.mokkery.test

import dev.mokkery.spy
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpyTest {

    private val realImpl = object : TestDependency {

        override var property: String = "1"

        override fun callWithPrimitives(i: Int, j: Int): Double = i.toDouble()

        override fun callWithComplex(list: List<String>): List<Int> = list.map { it.toInt() }

        override fun Int.callWithExtensionReceiver(): String = toString()

        override fun callWithVararg(i: Int, vararg args: String): Double = 1.0

        override suspend fun callWithSuspension(i: Int): List<String> = listOf(i.toString())

        override fun callUnit() = Unit

        override fun callNothing(): Nothing = throw IllegalArgumentException()
    }

    private val spied = spy<TestDependency>(realImpl)

    @Test
    fun testEveryMethodReturnsFromRealImpl() = runTest {
        assertEquals(1.0, spied.callWithPrimitives(1))
        assertEquals("1", spied.run { 1.callWithExtensionReceiver() })
        assertEquals(1.0, spied.callWithVararg(1, "2"))
        assertEquals(listOf("1"), spied.callWithSuspension(1))
        assertFailsWith<IllegalArgumentException> { spied.callNothing() }
    }

    @Test
    fun testRegistersMethodCalls() = runTest {
        spied.callWithPrimitives(1)
        spied.callWithSuspension(1)
        verifySuspend {
            spied.callWithPrimitives(1)
            spied.callWithSuspension(1)
        }
    }
}
