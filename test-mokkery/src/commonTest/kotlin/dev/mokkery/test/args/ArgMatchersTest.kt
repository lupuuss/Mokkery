package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher
import dev.mokkery.annotations.VarArgMatcherBuilder
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.getIfPresent
import dev.mokkery.matcher.capture.onArg
import dev.mokkery.matcher.capture.propagateCapture
import dev.mokkery.matcher.collections.contentDeepEq
import dev.mokkery.matcher.collections.contentEq
import dev.mokkery.matcher.collections.isIn
import dev.mokkery.matcher.collections.isNotIn
import dev.mokkery.matcher.eq
import dev.mokkery.matcher.eqRef
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.gte
import dev.mokkery.matcher.logical.LogicalMatchers
import dev.mokkery.matcher.logical.and
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.lt
import dev.mokkery.matcher.lte
import dev.mokkery.matcher.matches
import dev.mokkery.matcher.matchesComposite
import dev.mokkery.matcher.neq
import dev.mokkery.matcher.neqRef
import dev.mokkery.matcher.nullable.notNull
import dev.mokkery.matcher.ofType
import dev.mokkery.matcher.varargs.varargsIntAll
import dev.mokkery.mock
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.test.ComplexType
import dev.mokkery.test.externalMatcher
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(DelicateMokkeryApi::class)
class ArgMatchersTest {

    private val mock = mock<ComplexArgsInterface>()


    @Test
    fun testEqualityMatchers() {
        every { mock.callManyPrimitives(eq(1), neq(1.0)) } returns ComplexType
        assertEquals(ComplexType, mock.callManyPrimitives(1, 2.0))
        assertEquals(ComplexType, mock.callManyPrimitives(1, 3.0))
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(2, 2.0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(1, 1.0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(2, 1.0) }
    }

    @Test
    fun testRefEqualityMatchers() {
        val ref1 = ComplexType("a")
        val ref2 = ComplexType("a")
        every { mock.callComplex(neqRef(ref2)) } returns ComplexType
        every { mock.callComplex(eqRef(ref1)) } returns ref1
        assertEquals(ComplexType, mock.callComplex(ComplexType("a")))
        assertEquals(ref1, mock.callComplex(ref1))
        assertFailsWith<MokkeryRuntimeException> { mock.callComplex(ref2) }
    }

    @Test
    fun testComparisonMatchers() {
        every { mock.callPrimitive(lte(5)) } returns 5
        every { mock.callPrimitive(gte(7)) } returns 7
        assertEquals(5, mock.callPrimitive(5))
        assertEquals(5, mock.callPrimitive(4))
        assertEquals(7, mock.callPrimitive(7))
        assertEquals(7, mock.callPrimitive(8))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(6) }
    }

    @Test
    fun testDirectCompositeFunctionReference() {
        every { mock.callPrimitive(matchesComposite(1, 2, builder = LogicalMatchers::Or)) } returnsArgAt 0
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
    }

    @Test
    fun testDirectCompositeLambdaLiteral() {
        every {
            mock.callPrimitive(matchesComposite(1, 2) { LogicalMatchers.Or(it) })
        } returnsArgAt 0
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
    }

    @Test
    fun testDirectLambdaVariable() {
        val lambda =  { matchers: List<ArgMatcher<Int>> -> LogicalMatchers.Or(matchers) }
        every { mock.callPrimitive(matchesComposite(1, 2, builder = lambda)) } returnsArgAt 0
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
    }

    @Test
    fun testOfTypeMatcher() {
        every { mock.callComplex(ofType<ComplexType.Companion>()) } returns ComplexType
        assertEquals(ComplexType, mock.callComplex(ComplexType))
        assertFailsWith<MokkeryRuntimeException> { mock.callComplex(ComplexType("a")) }
    }

    @Test
    fun testAndMatcher() {
        every { mock.callPrimitive(and(neq(1), neq(2))) } returns 1
        assertEquals(1, mock.callPrimitive(0))
        assertEquals(1, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testOrMatcher() {
        every { mock.callPrimitive(or(eq(1), eq(2), eq(3), eq(4))) } returns 2
        assertEquals(2, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
        assertEquals(2, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(5) }
    }

    @Test
    fun testNotMatcher() {
        every { mock.callComplex(not(eqRef(ComplexType))) } returns ComplexType
        assertEquals(ComplexType, mock.callComplex(mock()))
        assertFailsWith<MokkeryRuntimeException> { mock.callComplex(ComplexType) }
    }

    @Test
    fun testContentDeepEqForArray() {
        every { mock.callNestedArray(contentDeepEq(arrayOf(arrayOf(1), arrayOf(2)))) } returns arrayOf(1, 2)
        assertContentEquals(arrayOf(1, 2), mock.callNestedArray(arrayOf(arrayOf(1), arrayOf(2))))
        assertFailsWith<MokkeryRuntimeException> { mock.callNestedArray(arrayOf()) }
    }

    @Test
    fun testContentEqForArray() {
        every { mock.callArray(contentEq(arrayOf(ComplexType))) } returns arrayOf(ComplexType)
        assertContentEquals(arrayOf(ComplexType), mock.callArray(arrayOf(ComplexType)))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callArray(arrayOf(ComplexType("a")))
        }
    }

    @Test
    fun testContentEqForIntArray() {
        every { mock.callIntArray(contentEq(intArrayOf(1, 2, 3))) } returns intArrayOf(6)
        assertContentEquals(intArrayOf(6), mock.callIntArray(intArrayOf(1, 2, 3)))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callIntArray(intArrayOf(1, 2, 3, 4))
        }
    }

    @Test
    fun testIsIn() {
        every { mock.callPrimitive(isIn(1, 2, 3)) } returns 1
        every { mock.callPrimitive(isIn(listOf(5, 6))) } returns 2
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(2))
        assertEquals(1, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(4) }
        assertEquals(2, mock.callPrimitive(5))
        assertEquals(2, mock.callPrimitive(6))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(7) }
    }

    @Test
    fun testIsNotIn() {
        every { mock.callPrimitive(isNotIn(1, 2)) } returns 1
        every { mock.callPrimitive(isNotIn(listOf(2, 3))) } returns 2
        assertEquals(2, mock.callPrimitive(0))
        assertEquals(2, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
        assertEquals(1, mock.callPrimitive(3))
        assertEquals(2, mock.callPrimitive(4))
    }

    @Test
    fun testCapture() {
        val slot = Capture.slot<Int>()
        every { mock.callManyPrimitives(capture(slot), 1.0) } returns ComplexType
        mock.callManyPrimitives(2, 1.0)
        mock.callManyPrimitives(3, 1.0)
        assertEquals(3, slot.getIfPresent())
    }

    @Test
    fun testCaptureWithNonDefaultMatcher() {
        val slot = Capture.slot<Int>()
        every { mock.callManyPrimitives(capture(slot, eq(2)), 1.0) } returns ComplexType
        mock.callManyPrimitives(2, 1.0)
        assertFailsWith<MokkeryRuntimeException> {
            mock.callManyPrimitives(3, 1.0)
        }
        assertEquals(2, slot.getIfPresent())
    }


    @Test
    fun testCaptureWithNotFullMatch() {
        val slot1 = Capture.slot<Int>()
        val slot2 = Capture.slot<Int>()
        every { mock.callManyPrimitives(capture(slot1), 1.0) } returns ComplexType
        every { mock.callManyPrimitives(capture(slot2), 2.0) } returns ComplexType
        mock.callManyPrimitives(2, 1.0)
        assertEquals(2, slot1.getIfPresent())
        assertEquals(null, slot2.getIfPresent())
    }

    @Test
    fun testCaptureCallbackIsCalled() {
        val called = mutableListOf<Int>()
        val callback = Capture.callback(callback = called::add)
        every { mock.callPrimitive(any()) } returns 1
        every { mock.callPrimitive(capture(callback, gte(1))) } returns 1
        mock.callPrimitive(0)
        mock.callPrimitive(1)
        mock.callPrimitive(3)
        assertEquals(listOf(1, 3), called)
    }

    @Test
    fun testOnArgCallbackIsCalled() {
        val called = mutableListOf<Int>()
        every { mock.callPrimitive(any()) } returns 1
        every { mock.callPrimitive(onArg(gte(1), called::add)) } returns 1
        mock.callPrimitive(0)
        mock.callPrimitive(1)
        mock.callPrimitive(3)
        assertEquals(listOf(1, 3), called)
    }

    @Test
    fun testNotNullMatcherAllowsUsingNotNullableMatchers() {
        every { mock.callNullable(any()) } returns 1
        every { mock.callNullable(notNull()) } returns 2
        every { mock.callNullable(notNull(gte(3))) } returns 3
        assertEquals(1, mock.callNullable(null))
        assertEquals(2, mock.callNullable(2))
        assertEquals(3, mock.callNullable(3))
    }

    @Test
    fun testCustomRegularMatcher() {
        every { mock.callNullable(any()) } returns 0
        every { mock.callNullable(isNull()) } returns 1
        assertEquals(1, mock.callNullable(null))
        assertEquals(0, mock.callNullable(1))
    }

    @Test
    fun testCustomCompositeMatcher() {
        every { mock.callNullable(customAnd(not(isNull()), gte(1))) } returns 3
        assertEquals(3, mock.callNullable(1))
        assertEquals(3, mock.callNullable(2))
        assertEquals(3, mock.callNullable(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callNullable(null) }
        assertFailsWith<MokkeryRuntimeException> { mock.callNullable(0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callNullable(-1) }
    }

    @Test
    fun testCustomCompositeWithDefaultMatcher() {
        val capture = Capture.container<Int?>()
        every { mock.callNullable(customCapture(capture)) } returns 3
        assertEquals(3, mock.callNullable(1))
        assertEquals(3, mock.callNullable(2))
        assertEquals(3, mock.callNullable(3))
        assertEquals(listOf(1, 2, 3), capture.values)
    }

    @Test
    fun testCustomMatcherInClass() {
        every { mock.callPrimitive(xor(gt(1), lt(3))) } returnsArgAt 0
        assertEquals(0, mock.callPrimitive(0))
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(4, mock.callPrimitive(4))
        assertEquals(5, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testValueArgMatchersScopeMatcher() {
        every { mock.callPrimitive(eqValueScope(this, 1)) } returns 2
        assertEquals(2, mock.callPrimitive(1))
    }

    @Test
    fun tesContextArgMatchersScopeMatcher() {
        every { mock.callPrimitive(eqContextScope(1)) } returns 2
        assertEquals(2, mock.callPrimitive(1))
    }

    @Test
    fun testInfixMatcherWithContext() {
        every { mock.callPrimitive(1 orr 2 orr 3 orr gte(4)) } returnsArgAt 0
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
        assertEquals(3, mock.callPrimitive(3))
        assertEquals(4, mock.callPrimitive(4))
        assertEquals(5, mock.callPrimitive(5))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(-1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
    }

    @Test
    fun testExternalGeneratedMatcher() {
        every { mock.callPrimitive(externalMatcher(1)) } returns 1
        assertEquals(1, mock.callPrimitive(1))
    }

    @Test
    fun testRegularValueMatcher() {
        every { mock.callPrimitive(regularValueMatcher()) } returns 2
        assertEquals(2, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testConditionalMatcher() {
        every { mock.callPrimitive(anyOrEq(false, 0)) } returns 3
        every { mock.callPrimitive(anyOrEq(true, 2)) } returns 2
        assertEquals(3, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
        assertEquals(3, mock.callPrimitive(3))
    }

    @Test
    fun testCustomVarargMatcher() {
        every { mock.callPrimitiveVarargs(1, 1, *varargsIntAllEq(2), 3) } returns 3
        assertEquals(3, mock.callPrimitiveVarargs(1, 1, 2, 2, 3))
        assertEquals(3, mock.callPrimitiveVarargs(1, 1, 2, 3))
        assertEquals(3, mock.callPrimitiveVarargs(1, 1, 3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 2, 2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 2, 2, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 1, 2, 2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1) }
    }

    @Test
    fun testCustomCompositeOfVarargMatcher() {
        every { mock.callPrimitiveVarargs(1, 1, *notVarargsIntAllEq(2), 3) } returns 3
        assertEquals(3, mock.callPrimitiveVarargs(1, 1, 1, 2, 3))
        assertEquals(3, mock.callPrimitiveVarargs(1, 1, 2, 3, 3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 1, 2, 2, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 1, 2, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 1, 3) }
    }

    @Test
    fun testRawMatcherParam() {
        every { mock.callPrimitive(rawMatcher(ArgMatcher.Equals(1))) } returns 1
        assertEquals(1, mock.callPrimitive(1))
    }


    private fun MokkeryMatcherScope.xor(@Matcher left: Int, @Matcher right: Int): Int = not(and(left, right))
}


private fun <T> MokkeryMatcherScope.isNull(): T = customMatcher { it == null }

private fun <T> MokkeryMatcherScope.customMatcher(block: (T) -> Boolean): T = matches { block(it) }

private fun <T> MokkeryMatcherScope.customCapture(
    capture: Capture<T>,
    @Matcher matcher: T = customMatcher { true }
) = capture(capture, matcher)

@OptIn(DelicateMokkeryApi::class)
private fun <T> MokkeryMatcherScope.customAnd(
    @Matcher first: T,
    @Matcher second: T,
    @Matcher vararg matchers: T
): T = matchesComposite(first, second, *matchers) {
    object : ArgMatcher.Composite<T> {
        override fun capture(value: T) = it.propagateCapture(value)

        override fun matches(arg: T): Boolean = it.all { matcher -> matcher.matches(arg) }

        override fun toString(): String = "customAnd(${it.joinToString()})"
    }
}

private fun <T> eqValueScope(scope: MokkeryMatcherScope, value: T): T = scope.matches { it == value }

context(scope: MokkeryMatcherScope)
private fun <T> eqContextScope(value: T): T = scope.matches { it == value }

context(scope: MokkeryMatcherScope)
private inline infix fun <reified T> @receiver:Matcher T.orr(@Matcher other: T): T = scope.or(this, other)

private fun MokkeryMatcherScope.regularValueMatcher(): Int = 1

private fun MokkeryMatcherScope.anyOrEq(
    condition: Boolean,
    value: Int
): Int = if (condition) value else any()

@VarArgMatcherBuilder
private fun MokkeryMatcherScope.varargsIntAllEq(value: Int): IntArray = varargsIntAll { it == value }

@VarArgMatcherBuilder
private fun MokkeryMatcherScope.notVarargsIntAllEq(value: Int): IntArray = not(varargsIntAllEq(value))

private fun MokkeryMatcherScope.rawMatcher(arg: ArgMatcher<Int>): Int = matches(arg)
