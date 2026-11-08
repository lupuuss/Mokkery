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
import dev.mokkery.matcher.logical.not
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
    fun testVarargMatchersSelectedByIfAreSpread() {
        every { mock.callPrimitive(anyOfOr(true, arrayOf(5), 1, gt(10))) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testLiteralArraySelectedByIfIsSpreadAsLiterals() {
        every { mock.callPrimitive(anyOfOr(false, arrayOf(5), 1, gt(10))) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(11) }
    }

    @Test
    fun testVarargMatchersInStatementPositionAreIgnored() {
        every { mock.callPrimitive(allOfAfterStatements(false, gt(1), gt(3))) } returns 1
        every { mock.callPrimitive(allOfAfterStatements(true, gt(1), gt(3))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testPrimitiveVarargMatchersSelectedByIf() {
        every { mock.callPrimitive(firstOfSelectedInts(true, gt(1), gt(5))) } returns 1
        every { mock.callPrimitive(firstOfSelectedInts(false, gt(10), gt(5))) } returns 2
        assertEquals(1, mock.callPrimitive(2))
        assertEquals(2, mock.callPrimitive(11))
    }

    @Test
    fun testUnsignedVarargMatchersSelectedByIf() {
        val consumer = mock<UIntConsumer>()
        every { consumer.consume(firstOfSelectedUInts(true, gt(1u), gt(5u))) } returns 1
        assertEquals(1, consumer.consume(2u))
        assertFailsWith<MokkeryRuntimeException> { consumer.consume(1u) }
    }

    @Test
    fun testVarargMatchersVariableReassignedWithLiterals() {
        every { mock.callPrimitive(anyOfReplaced(true, arrayOf(5), 1, gt(10))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(11) }
    }

    @Test
    fun testVarargMatchersSelectedByIfWithThrowingBranch() {
        every { mock.callPrimitive(anyOfOrFail(true, 1, gt(10))) } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testVarargMatchersVariableWithThrowingBranch() {
        every { mock.callPrimitive(anyOfOrFailCopied(true, 1, gt(10))) } returns 1
        assertEquals(1, mock.callPrimitive(11))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testNullableMaterializedValueSelectedByIf() {
        every { mock.callPrimitive(notIfNotNullSelectedByIf(false, gt(1))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testNullableMaterializedValueVariableAssignedNull() {
        every { mock.callPrimitive(notIfNotNullAssigned(gt(1))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testNullableMaterializedValueFromInlineLambda() {
        every { mock.callPrimitive(notIfNotNullFromRun(false, gt(1))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testLocalFunctionReturningNullableArgMatcher() {
        every { mock.callPrimitive(notIfNotNullFromLocalFunction(gt(1))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
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

    @Test
    fun testVariableReassignedAfterMaterializedInitializer() {
        every { mock.callPrimitive(gtAfterReassignment()) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testNullableMaterializedValueAbsentInVararg() {
        every { mock.callPrimitive(allOfOptional(gt(1))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testNullableMaterializedValuePresentInVararg() {
        every { mock.callPrimitive(allOfOptional(gt(1), materialize(gt(3)))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testNullableMaterializedVariableInVararg() {
        every { mock.callPrimitive(allOfNullVariable(gt(1))) } returns 1
        assertEquals(1, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
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

private fun <T> MokkeryMatcherScope.anyOfOr(
    useMatchers: Boolean,
    alternatives: Array<T>,
    @Matcher first: T,
    @Matcher vararg matchers: T
): T = or(first, first, *if (useMatchers) matchers else alternatives)

private fun <T> MokkeryMatcherScope.anyOfReplaced(
    replace: Boolean,
    replacement: Array<T>,
    @Matcher first: T,
    @Matcher vararg matchers: T
): T {
    var current = matchers
    if (replace) current = replacement
    return or(first, first, *current)
}

@OptIn(DelicateMokkeryApi::class)
private fun <T> MokkeryMatcherScope.anyOfCopied(@Matcher vararg matchers: T): T {
    val copy = matchers
    return matches(LogicalMatchers.Or(materialize(*copy)))
}

private fun <T> MokkeryMatcherScope.allOfAfterStatements(flag: Boolean, @Matcher vararg matchers: T): T {
    if (flag) matchers else Unit
    if (flag) {
        flag.toString()
        matchers
    }
    return allOf(*matchers)
}

private fun MokkeryMatcherScope.notIfNotNullSelectedByIf(flag: Boolean, @Matcher first: Int): Int {
    val other: ArgMatcher<Int>? = if (flag) materialize(gt(100)) else null
    return if (other == null) first else not(first)
}

private fun MokkeryMatcherScope.notIfNotNullAssigned(@Matcher first: Int): Int {
    var other: ArgMatcher<Int>? = materialize(gt(100))
    other = null
    return if (other == null) first else not(first)
}

private fun MokkeryMatcherScope.notIfNotNullFromRun(flag: Boolean, @Matcher first: Int): Int {
    val candidate = materialize(gt(100))
    val other: ArgMatcher<Int>? = run {
        if (flag) return@run candidate
        null
    }
    return if (other == null) first else not(first)
}

private fun MokkeryMatcherScope.notIfNotNullFromLocalFunction(@Matcher first: Int): Int {
    fun none(): ArgMatcher<Int>? = null
    return if (none() == null) first else not(first)
}

private fun <T> MokkeryMatcherScope.anyOfOrFail(
    useMatchers: Boolean,
    @Matcher first: T,
    @Matcher vararg matchers: T
): T {
    return or(first, first, *if (useMatchers) matchers else error("unused"))
}

private fun <T> MokkeryMatcherScope.anyOfOrFailCopied(
    useMatchers: Boolean,
    @Matcher first: T,
    @Matcher vararg matchers: T
): T {
    val selected = if (useMatchers) matchers else throw IllegalStateException()
    return or(first, first, *selected)
}

private fun MokkeryMatcherScope.firstOfInts(@Matcher first: Int, @Matcher vararg rest: Int): Int = first

private fun MokkeryMatcherScope.firstOfSelectedInts(
    flag: Boolean,
    @Matcher first: Int,
    @Matcher vararg rest: Int
): Int {
    val selected = if (flag) rest else intArrayOf(1)
    return firstOfInts(first, *selected)
}

private fun MokkeryMatcherScope.firstOfUInts(@Matcher first: UInt, @Matcher vararg rest: UInt): UInt = first

private fun MokkeryMatcherScope.firstOfSelectedUInts(
    flag: Boolean,
    @Matcher first: UInt,
    @Matcher vararg rest: UInt
): UInt {
    val selected = if (flag) rest else uintArrayOf(1u)
    return firstOfUInts(first, *selected)
}

private interface UIntConsumer {
    fun consume(value: UInt): Int
}

private fun MokkeryMatcherScope.gtAfterReassignment(): Int {
    var value: Any = materialize(gt(1))
    value = 5
    return gt(value as Int - 4)
}

@OptIn(DelicateMokkeryApi::class)
private fun MokkeryMatcherScope.allOfOptional(@Matcher first: Int, other: ArgMatcher<Int>? = null): Int =
    matches(LogicalMatchers.And(listOfNotNull(materialize(first), other)))

@OptIn(DelicateMokkeryApi::class)
private fun MokkeryMatcherScope.allOfNullVariable(@Matcher first: Int): Int {
    val other: ArgMatcher<Int>? = null
    return matches(LogicalMatchers.And(listOfNotNull(materialize(first), other)))
}
