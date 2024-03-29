package dev.mokkery.test

import dev.mokkery.spy
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpyTest {

    private val spied = spy<TestInterface>(TestInterfaceImpl)

    @Test
    fun testEveryMethodReturnsFromRealImpl() {
        assertEquals("1", spied.property)
        assertEquals(1.0, spied.callWithPrimitives(1))
        assertEquals(listOf(1), spied.callWithComplex(listOf("1")))
        assertEquals("1", spied.run { 1.callWithExtensionReceiver() })
        assertEquals(1.0, spied.callWithVararg(1, "2"))
        assertEquals(Unit, spied.callWithSelf(spied))
        assertFailsWith<IllegalArgumentException> { spied.callNothing() }
    }

    @Test
    fun testEverySuspendableMethodReturnsFromRealImpl() = runTest {
        assertEquals(listOf("1"), spied.callWithSuspension(1))
    }

    @Test
    fun testReturnsFromSpiedFunction() {
        val func = { i: Int -> i.toString() }
        val spied = spy(func)
        assertEquals("1", spied(1))
    }

    @Test
    fun testReturnsFromSpiedSuspendFunction() = runTest {
        val func: suspend (Int) -> String = {
            delay(1)
            it.toString()
        }
        val spied = spy(func)
        assertEquals("1", spied(1))
    }

    @Test
    fun testRegistersSpiedFunctionCall() {
        val func = { i: Int -> i.toString() }
        val spied = spy(func)
        assertEquals("1", spied(1))
        verify {
            spied(1)
        }
        assertFailsWith<AssertionError> {
            verify {
                spied(2)
            }
        }
    }

    @Test
    fun testRegistersSpiedSuspendFunctionCall() = runTest {
        val func: suspend (Int) -> String = {
            delay(1)
            it.toString()
        }
        val spied = spy(func)
        spied(1)
        verifySuspend {
            spied(1)
        }
        assertFailsWith<AssertionError> {
            verifySuspend {
                spied(2)
            }
        }
    }

    @Test
    fun testRegistersMethodCalls() = runTest {
        spied.callWithPrimitives(1)
        spied.callWithSuspension(1)
        runCatching { spied.callNothing() }
        verifySuspend {
            spied.callWithPrimitives(1)
            spied.callWithSuspension(1)
            spied.callNothing()
        }
    }
}

private object TestInterfaceImpl : TestInterface {

    override var property: String = "1"
    override fun callWithString(value: String?): Int = 0

    override fun callWithPrimitives(i: Int, j: Int): Double = i.toDouble()

    override fun callWithComplex(list: List<String>): List<Int> = list.map { it.toInt() }

    override fun Int.callWithExtensionReceiver(): String = toString()

    override fun callWithVararg(i: Int, vararg args: String): Double = 1.0

    override suspend fun callWithSuspension(i: Int): List<String> = listOf(i.toString())

    override fun callWithSelf(dependency: TestInterface) = Unit

    override fun callUnit() = Unit

    override fun callNothing(): Nothing = throw IllegalArgumentException()

    override fun <T> callGeneric(value: T): T where T : Comparable<T>, T : Number = value

    override val baseInterfaceProperty = "123"

    override fun baseInterfaceMethod() = Unit

    override fun <T> callWithArray(array: Array<T>): T = array.first()

    override fun callWithIntArray(array: IntArray): String = array.contentToString()
}
