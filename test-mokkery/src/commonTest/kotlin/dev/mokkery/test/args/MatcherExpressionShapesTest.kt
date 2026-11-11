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
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext
import dev.mokkery.verify
import dev.mokkery.test.ComplexArgsInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MatcherExpressionShapesTest {

    private val mock = mock<ComplexArgsInterface>()

    @Test
    fun testNamedArgument() {
        every { mock.callPrimitive(input = gt(1)) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testNamedArgumentsOfMatcherBuilder() {
        every { mock.callPrimitive(or(second = gt(10), first = 1)) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testParenthesizedMatcher() {
        every { mock.callPrimitive((gt(1))) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testLabeledMatcher() {
        every { mock.callPrimitive(label@ gt(1)) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testWhenWithSubjectSelectsMatcher() {
        val useMatcher = true
        every {
            mock.callPrimitive(
                when (useMatcher) {
                    true -> gt(1)
                    false -> 1
                }
            )
        } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testMatcherVariableInitializedWithWhen() {
        val useMatcher = false
        every {
            val matcher = when {
                useMatcher -> gt(1)
                else -> 5
            }
            mock.callPrimitive(matcher)
        } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testSafeCallOnNullableMock() {
        val nullableMock: ComplexArgsInterface? = mock
        every { nullableMock?.callPrimitive(gt(1)) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testElvisFallsBackToMatcher() {
        val value: Int? = null
        every { mock.callNullable(value ?: gt(1)) } returns 1
        assertEquals(1, mock.callNullable(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callNullable(1) }
    }

    @Test
    fun testElvisUsesValueAsLiteral() {
        val value: Int? = 3
        every { mock.callNullable(value ?: gt(1)) } returns 1
        assertEquals(1, mock.callNullable(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callNullable(5) }
    }

    @Test
    fun testCtxValueIsPassedToMatcherContextParameter() {
        every { ctx(gt(1)) { mock.callPrimitive(notContextMatcher()) } } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testCtxNamedArgumentsInReversedOrder() {
        every { ctx(block = { mock.callPrimitive(notContextMatcher()) }, a = gt(1)) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testCtxPlainValueIsPassedToMatcherContextParameter() {
        every { ctx(5) { mock.callPrimitive(notContextMatcher()) } } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testCtxAsArgument() {
        every { mock.callPrimitive(ctx(gt(1)) { notContextMatcher() }) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testCtxAsVariableInitializer() {
        every {
            val matcher = ctx(gt(1)) { notContextMatcher() }
            mock.callPrimitive(matcher)
        } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testCtxAsBranchValue() {
        val useCtx = true
        every { mock.callPrimitive(if (useCtx) ctx(gt(1)) { notContextMatcher() } else 7) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(7) }
    }

    @Test
    fun testCtxAsBranchValueNotSelected() {
        val useCtx = false
        every { mock.callPrimitive(if (useCtx) ctx(gt(1)) { notContextMatcher() } else 7) } returns 1
        assertEquals(1, mock.callPrimitive(7))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testSafeCallExtAsArgument() {
        val receiver: Int? = 1
        every { mock.callNullable(receiver?.ext { gt(this) }) } returns 1
        assertEquals(1, mock.callNullable(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callNullable(1) }
    }

    @Test
    fun testSafeCallExtOnNullAsArgument() {
        val receiver: Int? = null
        every { mock.callNullable(receiver?.ext { gt(this) }) } returns 1
        assertEquals(1, mock.callNullable(null))
        assertFailsWith<MokkeryRuntimeException> { mock.callNullable(2) }
    }

    @Test
    fun testNestedCtxAsArgument() {
        every { mock.callPrimitive(ctx(5) { ctx(gt(1)) { notContextMatcher() } }) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testCtxWithEarlyReturnedMatcherAsArgument() {
        val returnEarly = true
        every { mock.callPrimitive(ctx(1) { if (returnEarly) return@ctx gt(1); 2 }) } returns 1
        assertEquals(1, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testCtxWithEarlyUnitReturn() {
        val returnEarly = false
        every { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(1)
        verify { ctx(5) { if (returnEarly) return@ctx; mock.callPrimitive(1) } }
        assertFailsWith<AssertionError> { verify { ctx(5) { if (returnEarly) return@ctx; mock.callPrimitive(2) } } }
    }

    @Test
    fun testCtxWithPlainResultAsArgument() {
        every { mock.callPrimitive(ctx(gt(1)) { 2 } + 1) } returns 1
        assertEquals(1, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testExtWithPlainReceiverAsArgument() {
        every { mock.callPrimitive(2.ext { this }) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(3) }
    }

    @Test
    fun testExtWithMatcherReceiverAsArgument() {
        every { mock.callPrimitive(gt(1).ext { this }) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testCtxAsArgumentInVerify() {
        every { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(1)
        verify { mock.callPrimitive(ctx(gt(1)) { notContextMatcher() }) }
        assertFailsWith<AssertionError> { verify { mock.callPrimitive(ctx(gt(0)) { notContextMatcher() }) } }
    }
}

context(@Matcher matcher: Int)
private fun MokkeryMatcherScope.notContextMatcher(): Int = not(matcher)
