package dev.mokkery.internal.calls

import dev.mokkery.internal.MultipleMatchersForSingleArgException
import dev.mokkery.internal.VarargsAmbiguityDetectedException
import dev.mokkery.internal.matcher.CompositeVarArgMatcher
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.varargs.VarArgMatcher
import dev.mokkery.matcher.varargs.VarargMatcherMarker
import dev.mokkery.test.TestCompositeMatcher
import dev.mokkery.test.fakeCallArg
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ArgMatcherComposerTest {

    private val composer = ArgMatchersComposer()

    @Test
    fun testFailsOnMultipleRegularMatchersWithoutCompositeMatching() {
        assertFailsWith<MultipleMatchersForSingleArgException> {
            composer.compose(fakeCallArg(1), listOf(ArgMatcher.Equals(1), ArgMatcher.Equals(2)))
        }
    }

    @Test
    fun testFailsOnUnconsumedRegularMatchersWithComposite() {
        val matchers = listOf<ArgMatcher<Any?>>(
            ArgMatcher.Equals(1),
            ArgMatcher.Equals(2),
            ArgMatcher.Equals(3),
            TestCompositeMatcher(2)
        )
        assertFailsWith<MultipleMatchersForSingleArgException> {
            composer.compose(fakeCallArg(1), matchers)
        }
    }

    @Test
    fun testFailsOnAmbiguousVarargMatchers() {
        val matchers = listOf<ArgMatcher<Any?>>(
            ArgMatcher.Equals(1),
            ArgMatcher.Equals(2),
            TestCompositeMatcher(2)
        )
        assertFailsWith<VarargsAmbiguityDetectedException> {
            composer.compose(fakeCallArg(arrayOf(0, 0), isVararg = true), matchers)
        }
    }


    @Test
    fun testFailsOnAmbiguousVarargMatchersWithIncorrectlyConsumedVarargMatcher() {
        val matchers = listOf<ArgMatcher<Any?>>(
            ArgMatcher.Equals(1),
            VarArgMatcher.AnyOf(Int::class),
            TestCompositeMatcher(2)
        )
        assertFailsWith<VarargsAmbiguityDetectedException> {
            composer.compose(fakeCallArg(arrayOf(0, 0), isVararg = true), matchers)
        }
    }

    @Test
    fun testReturnsEqualsValueWhenNoMatcher() {
        assertEquals<ArgMatcher<Any?>>(ArgMatcher.Equals(2), composer.compose(fakeCallArg(2), emptyList()))
    }

    @Test
    fun testReturnsSingleMatcher() {
        assertEquals(ArgMatcher.Any, composer.compose(fakeCallArg(2), listOf(ArgMatcher.Any)))
    }

    @Test
    fun testReturnsProperlyComposedCompositeWhenFilled() {
        val matchers = listOf<ArgMatcher<Any?>>(ArgMatcher.Equals(1), ArgMatcher.Equals(2))
        val toCompose = matchers + TestCompositeMatcher(2)
        assertEquals(TestCompositeMatcher(2, matchers), composer.compose(fakeCallArg(1), toCompose))
    }

    @Test
    fun testMergesCompositesWhenMultiple() {
        val matchers = listOf<ArgMatcher<Any?>>(ArgMatcher.Equals(1), ArgMatcher.Equals(2))
        val toCompose = matchers + TestCompositeMatcher(2) +
                matchers + TestCompositeMatcher(2) +
                TestCompositeMatcher(2)
        val expected = TestCompositeMatcher(
            takes = 2,
            matchers = listOf(
                TestCompositeMatcher(2, matchers),
                TestCompositeMatcher(2, matchers),
            )
        )
        assertEquals(expected, composer.compose(fakeCallArg(1), toCompose))
    }

    @Test
    fun testValidatesComposites() {
        val matchers = listOf<ArgMatcher<Any?>>(
            ArgMatcher.Equals(1),
            ArgMatcher.Equals(2),
            TestCompositeMatcher(2)
        )
        val result = composer.compose(fakeCallArg(1), matchers)
        assertIs<TestCompositeMatcher<*>>(result)
        assertTrue(result.validated)
    }

    @Test
    fun testReturnsCompositeVarArgMatcherForVararg() {
        val matchers =
            listOf<ArgMatcher<Any?>>(ArgMatcher.Equals(1), VarArgMatcher.AnyOf(Int::class), ArgMatcher.Equals(2))
        val result = composer.compose(fakeCallArg(intArrayOf(0, 0, 0), isVararg = true), matchers)
        assertEquals(CompositeVarArgMatcher(Int::class, matchers), result)
    }

    @Test
    fun testProperlyMergesCompositeMatchersWithVararg() {
        val matchers = listOf<ArgMatcher<Any?>>(
            ArgMatcher.Equals(1),
            ArgMatcher.Equals(2),
            TestCompositeMatcher(2),
            VarArgMatcher.AnyOf(Int::class),
            ArgMatcher.Equals(3),
            ArgMatcher.Equals(4),
            TestCompositeMatcher(2)
        )
        val result = composer.compose(fakeCallArg(intArrayOf(0, 0, 0), isVararg = true), matchers)
        val expectedMatchers = listOf<ArgMatcher<Any?>>(
            TestCompositeMatcher(2, listOf(ArgMatcher.Equals(1), ArgMatcher.Equals(2))),
            VarArgMatcher.AnyOf(Int::class),
            TestCompositeMatcher(2, listOf(ArgMatcher.Equals(3), ArgMatcher.Equals(4)))
        )
        assertEquals(CompositeVarArgMatcher(Int::class, expectedMatchers), result)
    }

    @Test
    fun testProperlyMergesWildcardVarargMatchers() {
        val matchers = listOf<ArgMatcher<Any?>>(
            ArgMatcher.Equals(1),
            TestCompositeMatcher(1),
            VarArgMatcher.AnyOf(Int::class),
            TestCompositeMatcher(1),
            ArgMatcher.Equals(3),
            TestCompositeMatcher(1)
        )
        val result = composer.compose(fakeCallArg(intArrayOf(0, 0, 0), isVararg = true), matchers)
        val expectedMatchers = listOf<ArgMatcher<Any?>>(
            TestCompositeMatcher(1, listOf(ArgMatcher.Equals(1))),
            VarargMatcherMarker(TestCompositeMatcher(1, listOf(VarArgMatcher.AnyOf(Int::class)))),
            TestCompositeMatcher(1, listOf(ArgMatcher.Equals(3)))
        )
        assertEquals(CompositeVarArgMatcher(Int::class, expectedMatchers), result)
    }
}

