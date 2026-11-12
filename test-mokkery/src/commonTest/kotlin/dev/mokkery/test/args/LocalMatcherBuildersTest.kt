package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.annotations.Matcher
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.materialize
import dev.mokkery.mock
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext
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

    @Test
    fun testLocalBuilderComposingParamInsideTemplating() {
        every {
            fun MokkeryMatcherScope.local(@Matcher x: Int): Int = not(x)
            mock.callPrimitive(local(gt(5)))
        } returns 1
        assertMatchesNotGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderComposingParamInsideCtx() {
        every {
            ctx(5) {
                fun MokkeryMatcherScope.local(@Matcher x: Int): Int = not(x)
                mock.callPrimitive(local(gt(5)))
            }
        } returns 1
        assertMatchesNotGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderComposingParamInsideBuilder() {
        every { mock.callPrimitive(notGreaterThanFive()) } returns 1
        assertMatchesNotGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderPassingParamToOtherLocalBuilder() {
        every {
            fun MokkeryMatcherScope.first(@Matcher x: Int): Int = x
            fun MokkeryMatcherScope.second(@Matcher x: Int): Int = first(x)
            mock.callPrimitive(second(gt(5)))
        } returns 1
        assertMatchesGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderKeepingMatcherInVariableInsideTemplating() {
        every {
            fun MokkeryMatcherScope.local(@Matcher x: Int): Int {
                val negated = not(x)
                return negated
            }
            mock.callPrimitive(local(gt(5)))
        } returns 1
        assertMatchesNotGreaterThanFiveOnly()
    }

    @Test
    fun testLocalBuilderReassigningMatcherVariableInsideBuilder() {
        every { mock.callPrimitive(greaterThanFiveViaVariable()) } returns 1
        assertMatchesGreaterThanFiveOnly()
    }

    @Test
    fun testMemberBuilderOfLocalObjectInsideTemplating() {
        every {
            val builders = object {
                fun MokkeryMatcherScope.negated(@Matcher x: Int): Int = not(x)
            }
            builders.ext { mock.callPrimitive(negated(gt(5))) }
        } returns 1
        assertMatchesNotGreaterThanFiveOnly()
    }

    @Test
    fun testMatcherPropertyOfLocalObjectInsideTemplating() {
        every {
            val builders = object {
                val MokkeryMatcherScope.notGreaterThanFive: Int get() = not(gt(5))
            }
            builders.ext { mock.callPrimitive(notGreaterThanFive) }
        } returns 1
        assertMatchesNotGreaterThanFiveOnly()
    }

    private fun assertMatchesGreaterThanFiveOnly() {
        assertEquals(1, mock.callPrimitive(6))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(3) }
    }

    private fun assertMatchesNotGreaterThanFiveOnly() {
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(6) }
    }
}

private fun MokkeryMatcherScope.greaterThanFiveOrOne(flag: Boolean): Int {
    fun MokkeryMatcherScope.inner(@Matcher x: Int): Int = if (flag) x else 1
    return inner(gt(5))
}

private fun MokkeryMatcherScope.greaterThanFiveViaVariable(): Int {
    fun MokkeryMatcherScope.inner(): Int {
        var matcher = gt(1)
        matcher = gt(5)
        return matcher
    }
    return inner()
}

private fun MokkeryMatcherScope.notGreaterThanFive(): Int {
    fun MokkeryMatcherScope.inner(@Matcher x: Int): Int = not(x)
    return inner(gt(5))
}
