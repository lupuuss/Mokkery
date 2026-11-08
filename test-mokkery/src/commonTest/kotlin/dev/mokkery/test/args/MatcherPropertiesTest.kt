package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.annotations.Matcher
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.mock
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MatcherPropertiesTest {

    private val mock = mock<ComplexArgsInterface>()

    @Test
    fun testExtensionProperty() {
        every { mock.callPrimitive(greaterThanOne) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testContextProperty() {
        every { mock.callPrimitive(contextGreaterThanOne) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testPropertyPassedToMatcher() {
        every { mock.callPrimitive(not(greaterThanOne)) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testPropertyComposedFromProperties() {
        every { mock.callPrimitive(oneOrGreaterThanTen) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testPropertyUsedInMatcherBuilder() {
        every { mock.callPrimitive(greaterThanOneOr(0)) } returns 1
        assertEquals(1, mock.callPrimitive(0))
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testPropertyInVerify() {
        every { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(2)
        verify { mock.callPrimitive(greaterThanOne) }
    }

    @Test
    fun testMatcherPassedToReceiverMatcherProperty() {
        every { mock.callPrimitive(gt(1).negated) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testLiteralPassedToReceiverMatcherProperty() {
        every { mock.callPrimitive(5.negated) } returns 1
        assertEquals(1, mock.callPrimitive(4))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }
}

private val MokkeryMatcherScope.greaterThanOne: Int
    get() = gt(1)

context(scope: MokkeryMatcherScope)
private val contextGreaterThanOne: Int
    get() = scope.gt(1)

private val MokkeryMatcherScope.greaterThanTen: Int
    get() = gt(10)

private val MokkeryMatcherScope.oneOrGreaterThanTen: Int
    get() = or(1, greaterThanTen)

private fun MokkeryMatcherScope.greaterThanOneOr(@Matcher matcher: Int): Int = or(greaterThanOne, matcher)

context(scope: MokkeryMatcherScope)
private val @receiver:Matcher Int.negated: Int
    get() = scope.not(this)
