package dev.mokkery.test

import dev.mokkery.spy
import dev.mokkery.verifySuspend
import kotlin.test.Test
import kotlin.test.assertEquals

class SpyGenericTest {

    private val spied = spy<TestGenericInterface<String>>(TestInterfaceGenericImpl)

    @Test
    fun testEveryMethodReturnsFromRealImpl() {
        assertEquals("1", spied.value)
        assertEquals(true, spied.call("123"))
        assertEquals(2, spied.callBoundedGeneric(2))
        spied.run {
            assertEquals("[1]", listOf(1).genericExtension())
            assertEquals("[1, 2]", listOf("1", "2").extension())
            assertEquals(1, listOf(1).genericListSize)
            assertEquals(2, listOf("1", "2").listSize)
        }
    }

    @Test
    fun testEverySuspendableMethodReturnsFromRealImpl() = runTest {
        assertEquals(2.0, spied.callSuspendBoundedGeneric(2.0))
    }

    @Test
    fun testRegistersMethodCalls() = runTest {
        assertEquals(true, spied.call("123"))
        assertEquals(2.0, spied.callSuspendBoundedGeneric(2.0))
        verifySuspend {
            spied.call("123")
            spied.callSuspendBoundedGeneric(2.0)
        }
    }
}

object TestInterfaceGenericImpl : TestGenericInterface<String> {
    override val value: String = "1"

    override fun <T> callBoundedGeneric(value: T): T where T : Comparable<T>, T : Number = value

    override fun <T> callGeneric(value: T): T = value

    override suspend fun <T> callSuspendBoundedGeneric(value: T): T where T : Comparable<T>, T : Number = value

    override fun <T> List<Comparable<T>>.genericExtension(): String = toString()

    override val <T> List<T>.genericListSize: Int get() = size
    override val List<String>.listSize: Int get() = size

    override fun List<String>.extension(): String = toString()

    override fun call(value: String): Boolean = value.isNotEmpty()

}
