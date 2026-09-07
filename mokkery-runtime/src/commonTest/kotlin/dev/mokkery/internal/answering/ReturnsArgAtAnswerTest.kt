package dev.mokkery.internal.answering

import dev.mokkery.internal.ArgIndexOutOfBoundsException
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.testBlockingCallScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReturnsArgAtAnswerTest {

    private val scope = testBlockingCallScope<Int>(
        args = listOf(fakeCallArg(1, "i"), fakeCallArg(2, "j"))
    )

    @Test
    fun testReturnsArgumentAtGivenIndex() {
        assertEquals(1, ReturnsArgAtAnswer<Int>(0).call(scope))
        assertEquals(2, ReturnsArgAtAnswer<Int>(1).call(scope))
    }

    @Test
    fun testFailsWithArgIndexOutOfBoundsWhenIndexExceedsArgsCount() {
        assertFailsWith<ArgIndexOutOfBoundsException> { ReturnsArgAtAnswer<Int>(2).call(scope) }
    }

    @Test
    fun testFailsWithArgIndexOutOfBoundsWhenIndexIsNegative() {
        assertFailsWith<ArgIndexOutOfBoundsException> { ReturnsArgAtAnswer<Int>(-1).call(scope) }
    }

    @Test
    fun testFailsWithArgIndexOutOfBoundsWhenCallHasNoArgs() {
        val noArgsScope = testBlockingCallScope<Int>()
        assertFailsWith<ArgIndexOutOfBoundsException> { ReturnsArgAtAnswer<Int>(0).call(noArgsScope) }
    }
}
