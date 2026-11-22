package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.annotations.Matcher
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.lt
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.mock
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext
import dev.mokkery.verify
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.test.RegularMethodsInterface
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
    fun testCtxWithEarlyValueReturn() {
        val returnEarly = false
        every { ctx(5) { if (returnEarly) return@ctx 0; mock.callPrimitive(gt(1)) } } returns 3
        assertEquals(3, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testCtxWithEarlyReturnOfAnyResult() {
        val returnEarly = false
        every { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(1)
        verify { ctx<Int, Any?>(5) { if (returnEarly) return@ctx Unit; mock.callPrimitive(1) } }
        assertFailsWith<AssertionError> { verify { ctx<Int, Any?>(5) { if (returnEarly) return@ctx Unit; mock.callPrimitive(2) } } }
    }

    @Test
    fun testIfWithPlainBranchAsResult() {
        val plain = false
        every { if (plain) 0 else mock.callPrimitive(gt(1)) } returns 3
        assertEquals(3, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testTryWithPlainCatchAsResult() {
        every {
            try {
                mock.callPrimitive(gt(1))
            } catch (e: IllegalStateException) {
                0
            }
        } returns 3
        assertEquals(3, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
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
    fun testContextOfInCtx() {
        every { ctx(gt(1)) { mock.callPrimitive(contextOf<Int>()) } } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testContextOfInMatcherBuilder() {
        every { ctx(gt(1)) { mock.callPrimitive(notViaContextOf()) } } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testCtxAsArgumentInVerify() {
        every { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(1)
        verify { mock.callPrimitive(ctx(gt(1)) { notContextMatcher() }) }
        assertFailsWith<AssertionError> { verify { mock.callPrimitive(ctx(gt(0)) { notContextMatcher() }) } }
    }

    @Test
    fun testMatcherFromTryAsArgument() {
        every { mock.callPrimitive(try { gt(10) } catch (e: Exception) { 0 }) } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
    }

    @Test
    fun testPlainValueFromCatchAsArgument() {
        every { mock.callPrimitive(try { throwingValue(); gt(10) } catch (e: Exception) { 5 }) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(11) }
    }

    @Test
    fun testMatcherFromTryInVariable() {
        every {
            val matcher = try { gt(10) } finally { }
            mock.callPrimitive(not(matcher))
        } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(11) }
    }

    @Test
    fun testMatcherFromTryInMatcherBuilder() {
        every { mock.callPrimitive(gtFromTry()) } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
    }

    @Test
    fun testPlainValueFromCatchInMatcherBuilder() {
        every { mock.callPrimitive(plainFromCatch()) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(11) }
    }

    @Test
    fun testReturnFromTryInMatcherBuilder() {
        every { mock.callPrimitive(returnFromTry(true)) } returns 1
        every { mock.callPrimitive(returnFromTry(false)) } returns 2
        assertEquals(1, mock.callPrimitive(11))
        assertEquals(2, mock.callPrimitive(5))
    }

    @Test
    fun testReturnFromTryInCtx() {
        val flag = true
        every {
            mock.callPrimitive(ctx(1) {
                try {
                    if (flag) return@ctx gt(10)
                    2
                } catch (e: Exception) {
                    0
                }
            })
        } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testArrayFromTrySpreadIntoVararg() {
        every { mock.callPrimitiveVarargs(1, *try { intArrayOf(7, 2) } catch (e: Exception) { intArrayOf() }) } returns 1
        assertEquals(1, mock.callPrimitiveVarargs(1, 7, 2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 7, 3) }
    }

    @Test
    fun testTryAsCtxResult() {
        every { mock.callPrimitive(ctx(1) { try { gt(10) } catch (e: Exception) { 0 } }) } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
    }

    @Test
    fun testTryAsExtResult() {
        every { mock.callPrimitive(1.ext { try { gt(10) } catch (e: Exception) { this } }) } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testTryInsideIf() {
        val flag = true
        every { mock.callPrimitive(if (flag) try { gt(10) } catch (e: Exception) { 0 } else 5) } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testIfInsideTry() {
        val flag = false
        every { mock.callPrimitive(try { if (flag) gt(10) else lt(3) } catch (e: Exception) { 0 }) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(11) }
    }

    @Test
    fun testMatcherReassignedInsideTry() {
        every {
            var matcher = gt(10)
            try { matcher = lt(3) } catch (e: Exception) { }
            mock.callPrimitive(matcher)
        } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(11) }
    }

    @Test
    fun testVarargMatcherParamSpreadFromTryInMatcherBuilder() {
        every { mock.callPrimitive(orFromTry(1, gt(10))) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testMatcherParamFromTryInMatcherBuilder() {
        every { mock.callPrimitive(paramFromTry(gt(10))) } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testMatcherFromTryAsVarargElement() {
        every { mock.callPrimitiveVarargs(1, try { gt(10) } catch (e: Exception) { 0 }) } returns 1
        assertEquals(1, mock.callPrimitiveVarargs(1, 11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 5) }
    }

    @Test
    fun testBlockBodiedUnitBuilder() {
        val unitMock = mock<RegularMethodsInterface>()
        every { unitMock.callUnit(unitBuilder()) } returns Unit
        assertEquals(Unit, unitMock.callUnit(Unit))
    }
}

private fun MokkeryMatcherScope.unitBuilder() {}

private fun throwingValue(): Int = error("fail")

private fun MokkeryMatcherScope.gtFromTry(): Int = try {
    gt(10)
} catch (e: Exception) {
    0
}

private fun MokkeryMatcherScope.plainFromCatch(): Int = try {
    throwingValue()
    gt(10)
} catch (e: Exception) {
    5
}

private fun MokkeryMatcherScope.returnFromTry(flag: Boolean): Int {
    try {
        if (flag) return gt(10)
    } catch (e: Exception) {
        return 0
    }
    return 5
}

context(@Matcher matcher: Int)
private fun MokkeryMatcherScope.notContextMatcher(): Int = not(matcher)

context(@Matcher matcher: Int)
private fun MokkeryMatcherScope.notViaContextOf(): Int = not(contextOf<Int>())

private fun <T> MokkeryMatcherScope.orFromTry(@Matcher first: T, @Matcher vararg others: T): T =
    or(first, first, *try { others } catch (e: Exception) { others })

private fun <T> MokkeryMatcherScope.paramFromTry(@Matcher matcher: T): T = try { matcher } catch (e: Exception) { matcher }
