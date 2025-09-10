package dev.mokkery.test.answers

import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.spy
import dev.mokkery.test.ComplexType
import dev.mokkery.test.SpyTestInterface
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SpiedCallAnswersTest {

    private val obj = SpyTestInterface.Companion<Int?>()
    private val spy = spy<SpyTestInterface<Int?>>(obj)

    @Test
    fun testCallSpied() {
        var counter = 0
        every { spy.call(any()) } calls {
            counter++
            callSpied()
        }
        assertEquals(ComplexType("2"), spy.call(ComplexType("1")))
        assertEquals(1, counter)
    }

    @Test
    fun testCallSpiedWith() {
        var counter = 0
        every { spy.call(any()) } calls {
            counter++
            callSpiedWith(ComplexType("2"))
        }
        assertEquals(ComplexType("3"), spy.call(ComplexType("1")))
        assertEquals(1, counter)
    }

    @Test
    fun testCallSpiedSuspend() = runTest {
        var counter = 0
        everySuspend { spy.callSuspend(any()) } calls {
            counter++
            callSpied()
        }
        assertEquals(1, spy.callSuspend(1))
        assertEquals(1, counter)
    }

    @Test
    fun testCallSpiedWithSuspend() = runTest {
        var counter = 0
        everySuspend { spy.callSuspend(any()) } calls { (value: Int, flag: Boolean) ->
            counter++
            callSpiedWith(2, flag)
        }
        assertEquals(2, spy.callSuspend(1))
        assertEquals(1, counter)
    }
}
