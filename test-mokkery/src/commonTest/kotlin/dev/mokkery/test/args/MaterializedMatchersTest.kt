package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.logical.LogicalMatchers
import dev.mokkery.matcher.logical.and
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.matches
import dev.mokkery.matcher.materialize
import dev.mokkery.mock
import dev.mokkery.test.ComplexArgsInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

class MaterializedMatchersTest {

    private val mock = mock<ComplexArgsInterface>()

    @Test
    fun testHandWrittenCompositeWithMatchersAndLiterals() {
        every { mock.callPrimitive(either(gt(10), 1)) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testHandWrittenCompositePropagatesCaptures() {
        val container = Capture.container<Int>()
        every { mock.callPrimitive(either(capture(container), 1)) } returns 1
        mock.callPrimitive(1)
        mock.callPrimitive(2)
        assertEquals(listOf(1, 2), container.values)
    }

    @Test
    fun testMatcherWithMatcherParamsReturningNonComposite() {
        every { mock.callPrimitive(evenAnd(gt(2))) } returns 1
        assertEquals(1, mock.callPrimitive(4))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testLiteralArraySpreadIntoMatcherVararg() {
        every { mock.callPrimitive(or(1, 2, *arrayOf(3, 4))) } returns 1
        assertEquals(1, mock.callPrimitive(4))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testMaterializeVarargParam() {
        every { mock.callPrimitive(allOf(gt(1), matches { it % 2 == 0 }, 4)) } returns 1
        assertEquals(1, mock.callPrimitive(4))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testMaterializeMixingParamsLiteralsAndSpreads() {
        every { mock.callPrimitive(noneOf(1, gt(10), 3, 5)) } returns 1
        assertEquals(1, mock.callPrimitive(4))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(11) }
    }

    @Test
    fun testVarargParamCopiedToVariable() {
        every { mock.callPrimitive(allOfCopied(gt(1), 4)) } returns 1
        every { mock.callPrimitive(anyOfCopied(1, 2)) } returns 2
        assertEquals(1, mock.callPrimitive(4))
        assertEquals(2, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(3) }
    }

    @Test
    fun testMatcherObjectsAccessibleInTemplating() {
        var matcher: ArgMatcher<Int>? = null
        var literal: ArgMatcher<Int>? = null
        var fromArray: List<ArgMatcher<Int>>? = null
        var fromVararg: List<ArgMatcher<Int>>? = null
        var empty: List<ArgMatcher<Int>>? = null
        every {
            matcher = materialize(any<Int>())
            val three = materialize(3)
            literal = three
            fromArray = materialize(*arrayOf(1, 2))
            fromVararg = materialize(1, any(), *arrayOf(2, 3))
            empty = materialize()
            mock.callPrimitive(matches(three))
        } returns 1
        assertIs<ArgMatcher.Any>(matcher)
        assertEquals(ArgMatcher.Equals(3), literal)
        assertEquals(listOf(ArgMatcher.Equals(1), ArgMatcher.Equals(2)), fromArray)
        assertEquals(listOf(ArgMatcher.Equals(1), ArgMatcher.Any, ArgMatcher.Equals(2), ArgMatcher.Equals(3)), fromVararg)
        assertEquals(emptyList(), empty)
        assertEquals(1, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(4) }
    }

    @Test
    fun testMaterializeIsInverseOfMatches() {
        val argMatcher = ArgMatcher.Equals(3)
        var materialized: ArgMatcher<Int>? = null
        every {
            materialized = materialize(matches(argMatcher))
            mock.callPrimitive(1)
        } returns 1
        assertSame(argMatcher, materialized)
    }
}

@OptIn(DelicateMokkeryApi::class)
private fun <T> MokkeryMatcherScope.either(@Matcher first: T, @Matcher second: T): T {
    return matches(LogicalMatchers.Or(listOf(materialize(first), materialize(second))))
}

private fun MokkeryMatcherScope.evenAnd(@Matcher matcher: Int): Int {
    val argMatcher = materialize(matcher)
    return matches { it % 2 == 0 && argMatcher.matches(it) }
}

@OptIn(DelicateMokkeryApi::class)
private fun <T> MokkeryMatcherScope.allOf(@Matcher vararg matchers: T): T {
    return matches(LogicalMatchers.And(materialize(*matchers)))
}

@OptIn(DelicateMokkeryApi::class)
private fun <T> MokkeryMatcherScope.noneOf(@Matcher first: T, @Matcher vararg rest: T): T {
    return matches(LogicalMatchers.Not(materialize(first, *rest)))
}

@OptIn(DelicateMokkeryApi::class)
private fun <T> MokkeryMatcherScope.allOfCopied(@Matcher vararg matchers: T): T {
    val copy = matchers
    return matches(LogicalMatchers.And(materialize(*copy)))
}

@OptIn(DelicateMokkeryApi::class)
private fun <T> MokkeryMatcherScope.anyOfCopied(@Matcher vararg matchers: T): T {
    val copy = matchers
    return matches(LogicalMatchers.Or(materialize(*copy)))
}
