package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.annotations.Matcher
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.materialize
import dev.mokkery.mock
import dev.mokkery.templating.ctx
import dev.mokkery.test.ComplexArgsInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LocalMatcherBuildersTest {

    private val mock = mock<ComplexArgsInterface>()

    @Test
    fun testLocalBuilderInsideBuilder() {
        every { mock.callPrimitive(greaterThanFiveOrOne(true)) } returns 1
        assertMatchesGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderInsideTemplating() {
        every {
            fun MokkeryMatcherScope.local(@Matcher x: Int, flag: Boolean): Int = if (flag) x else 1
            mock.callPrimitive(local(gt(5), true))
        } returns 1
        assertMatchesGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderInsideCtx() {
        every {
            ctx(5) {
                fun MokkeryMatcherScope.local(@Matcher x: Int, flag: Boolean): Int = if (flag) x else 1
                mock.callPrimitive(local(gt(5), true))
            }
        } returns 1
        assertMatchesGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderInsideTemplatingUsingMaterializedMatcher() {
        every {
            fun MokkeryMatcherScope.local(@Matcher x: Int): Int = if (materialize(x).matches(6)) x else 1
            mock.callPrimitive(local(gt(5)))
        } returns 1
        assertMatchesGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderInsideTemplatingCallingOtherMock() {
        val other = mock<ComplexArgsInterface> { every { callPrimitive(any()) } returns 6 }
        every {
            fun MokkeryMatcherScope.local(@Matcher x: Int): Int = if (other.callPrimitive(0) == 6) x else 1
            mock.callPrimitive(local(gt(5)))
        } returns 1
        assertMatchesGreaterThanFiveOnly()
    }

    private fun assertMatchesGreaterThanFiveOnly() {
        assertEquals(1, mock.callPrimitive(6))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(3) }
    }
}

private fun MokkeryMatcherScope.greaterThanFiveOrOne(flag: Boolean): Int {
    fun MokkeryMatcherScope.inner(@Matcher x: Int): Int = if (flag) x else 1
    return inner(gt(5))
}
