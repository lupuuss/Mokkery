package dev.mokkery.test

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.register
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.spy
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpyTest {


    init {
        AutofillProvider.forInternals.types.register { PrimitiveValueClass(0) }
        AutofillProvider.forInternals.types.register { ValueClass(null) }
    }

    @Test
    fun testEveryMethodReturnsFromRealImpl() {
        val spied = spy<TestInterface>(TestInterfaceImpl)
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
        val spied = spy<TestInterface>(TestInterfaceImpl)
        assertEquals(listOf("1"), spied.callWithSuspension(1))
    }

    @Test
    fun testChangesBehaviourOfRegularMethodWithFallbackToSpiedObject() {
        val spied = spy<TestInterface>(TestInterfaceImpl) {
            every { property } returns "changedProperty"
            every { callGeneric(any<Int>()) } calls { (i: Int) -> i + 1 }
        }
        every { spied.callNothing() } throws NumberFormatException()
        every { spied.run { 1.callWithExtensionReceiver() } } returns "changedCallWithExtensionReceiver"
        assertEquals("changedProperty", spied.property)
        assertEquals(4, spied.callGeneric(3))
        assertEquals("changedCallWithExtensionReceiver", spied.run { 1.callWithExtensionReceiver() })
        assertFailsWith<NumberFormatException> { spied.callNothing() }
        // fallback
        assertEquals("2", spied.run { 2.callWithExtensionReceiver() })
    }

    @Test
    fun testChangesBehaviourOfSuspendMethod() = runTest {
        val spied = spy<TestInterface>(TestInterfaceImpl)
        everySuspend { spied.callWithSuspension(3) } returns listOf("callWithSuspension")
        assertEquals(listOf("callWithSuspension"), spied.callWithSuspension(3))
        // fallback
        assertEquals(listOf("2"), spied.callWithSuspension(2))
    }

    @Test
    fun testReturnsFromSpiedFunction() {
        val func = { i: Int -> i.toString() }
        val spied = spy(func)
        assertEquals("1", spied(1))
    }

    @Test
    fun testReturnsFromSpiedFunctionWithExtensionReceiver() {
        val func: Int.() -> String = { (this + 1).toString() }
        val spied = spy(func)
        assertEquals("2", spied(1))
    }

    @Test
    fun testChangesBehaviourOfRegularFunctionWithFallbackToSpied() {
        val func = { i: Int -> i.toString() }
        val spied = spy(func) {
            every { invoke(1) } returns "changed"
        }
        assertEquals("changed", spied(1))
        // fallback
        assertEquals("2", spied(2))
    }

    @Test
    fun testReturnsFromSpiedSuspendFunction() = runTest {
        val func: suspend (Int) -> String = { it.toString() }
        val spied = spy(func)
        assertEquals("1", spied(1))
    }


    @Test
    fun testChangesBehaviourOfSuspendFunctionWithFallbackToSpied() = runTest {
        val func: suspend (Int) -> String = { it.toString() }
        val spied = spy(func)
        everySuspend { spied(1) } returns "changed"
        assertEquals("changed", spied(1))
        // fallback
        assertEquals("2", spied(2))
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
        val func: suspend (Int) -> String = { it.toString() }
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
        val spied = spy<TestInterface>(TestInterfaceImpl)
        spied.callWithPrimitives(1)
        spied.callWithSuspension(1)
        spied.callWithComplexResult(Result.success(listOf(1)))
        spied.callWithPrimitiveValueClass(PrimitiveValueClass(1))
        runCatching { spied.callNothing() }
        verifySuspend {
            spied.callWithComplexResult(any())
            spied.callWithPrimitiveValueClass(any())
            spied.callWithPrimitives(1)
            spied.callWithSuspension(1)
            spied.callNothing()
        }
    }
}

private object TestInterfaceImpl : TestInterface {

    override var property: String = "1"

    override val <R> R.baseInterfaceGenericProperty: R
        get() = this

    override fun callWithString(value: String?): Int = 0

    override fun callWithPrimitives(i: Int, j: Int): Double = i.toDouble()

    override fun callWithComplex(list: List<String>): List<Int> = list.map { it.toInt() }

    override fun Int.callWithExtensionReceiver(): String = toString()

    override fun callWithVararg(i: Int, vararg args: String): Double = 1.0

    override suspend fun callWithSuspension(i: Int): List<String> = listOf(i.toString())

    override suspend fun callUnitWithSuspension(i: Int) = Unit

    override fun callWithSelf(dependency: TestInterface) = Unit

    override fun callUnit() = Unit

    override fun callNothing(): Nothing = throw IllegalArgumentException()

    override fun callWithPrimitiveResult(value: Result<Int>): Result<Int> = value

    override fun callWithComplexResult(value: Result<List<Int>>): Result<List<Int>> = value

    override fun callWithPrimitiveValueClass(value: PrimitiveValueClass): PrimitiveValueClass = value

    override fun callWithComplexValueClass(value: ValueClass<List<String>>): ValueClass<List<String>> = value

    override fun <T> callGeneric(value: T): T where T : Comparable<T>, T : Number = value

    override val baseInterfaceProperty = "123"

    override fun baseInterfaceMethod() = Unit

    override fun <R> baseCallWithGeneric(value: R): R = value

    override fun <T> callWithArray(array: Array<T>): T = array.first()

    override fun callWithIntArray(array: IntArray): String = array.contentToString()
}
