package dev.mokkery.test

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryScope
import dev.mokkery.MokkerySuiteScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.spy
import dev.mokkery.templating.ctx
import dev.mokkery.templating.ext
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MokkeryTypesInMockedMembersTest {

    private val eq5: ArgMatcher<Int> = ArgMatcher.Equals(5)
    private val eq6: ArgMatcher<Int> = ArgMatcher.Equals(6)

    @Test
    fun testArgMatcherParamIsComparedAsValue() {
        val mock = mock<ArgMatcherMembers>()
        every { mock.call(any<ArgMatcher<Int>>()) } returns 1
        every { mock.call(eq5) } returns 2
        assertEquals(2, mock.call(eq5))
        assertEquals(1, mock.call(eq6))
        verify { mock.call(eq5) }
    }

    @Test
    fun testArgMatcherParamAcceptsMatchers() {
        val mock = mock<ArgMatcherMembers>()
        every { mock.call(any<ArgMatcher<Int>>()) } returns 0
        every { mock.call(matches<ArgMatcher<Int>> { it.matches(5) }) } returns 1
        assertEquals(1, mock.call(eq5))
        assertEquals(0, mock.call(eq6))
    }

    @Test
    fun testArgMatcherVarargs() {
        val mock = mock<ArgMatcherMembers>()
        every { mock.callVararg(eq5, *any()) } returns 1
        assertEquals(1, mock.callVararg(eq5, eq6))
        verify { mock.callVararg(eq5, eq6) }
    }

    @Test
    fun testArgMatcherReturnAndProperty() {
        val mock = mock<ArgMatcherMembers>()
        every { mock.make() } returns eq5
        every { mock.matcher } returns eq6
        assertSame(eq5, mock.make())
        assertSame(eq6, mock.matcher)
    }

    @Test
    fun testArgMatcherTypeArgument() {
        val mock = mock<GenericMember<ArgMatcher<Int>>>()
        every { mock.call(eq5) } returns 1
        every { mock.call(eq6) } returns 2
        assertEquals(1, mock.call(eq5))
        assertEquals(2, mock.call(eq6))
    }

    @Test
    fun testMockedArgMatcher() {
        val matcher = mock<ArgMatcher<Int>>()
        every { matcher.matches(5) } returns true
        every { matcher.matches(6) } returns false
        assertTrue(matcher.matches(5))
        assertFalse(matcher.matches(6))
    }

    @Test
    fun testMockedArgMatcherPassedToMock() {
        val matcher = mock<ArgMatcher<Int>>()
        val mock = mock<ArgMatcherMembers>()
        every { mock.call(matcher) } returns 1
        every { mock.call(eq5) } returns 2
        assertEquals(1, mock.call(matcher))
        assertEquals(2, mock.call(eq5))
    }

    @Test
    fun testSpiedArgMatcher() {
        val spied = spy(eq5)
        assertTrue(spied.matches(5))
        every { spied.matches(6) } returns true
        assertTrue(spied.matches(6))
        assertFalse(spied.matches(7))
    }

    @Test
    fun testScopeParam() {
        val scope = MokkerySuiteScope()
        val other = MokkerySuiteScope()
        val mock = mock<ScopeMembers>()
        every { mock.call(any<MokkerySuiteScope>()) } returns 1
        every { mock.call(scope) } returns 2
        assertEquals(2, mock.call(scope))
        assertEquals(1, mock.call(other))
        verify { mock.call(scope) }
    }

    @Test
    fun testScopeReceiver() {
        val scope = MokkerySuiteScope()
        val mock = mock<ScopeMembers>()
        every { mock.ext { scope.callWithReceiver() } } returns 4
        assertEquals(4, with(mock) { scope.callWithReceiver() })
    }

    @OptIn(DelicateMokkeryApi::class)
    @Test
    fun testMockedAnswerReceivesCallScope() {
        val answer = mock<Answer<Int>>()
        val mock = mock<ArgMatcherMembers>()
        every { answer.call(any<MokkeryBlockingCallScope>()) } returns 7
        every { mock.call(eq5) } answers answer
        assertEquals(7, mock.call(eq5))
        verify { answer.call(any<MokkeryBlockingCallScope>()) }
    }

    @OptIn(DelicateMokkeryApi::class)
    @Test
    fun testScopeContextParameter() {
        val answer = mock<Answer<Int>>()
        val scoped = mock<ScopeMembers>()
        val mock = mock<ArgMatcherMembers>()
        every { ctx(any<MokkeryCallScope>()) { scoped.callWithContext() } } returns 8
        every { answer.call(any<MokkeryBlockingCallScope>()) } calls { (scope: MokkeryBlockingCallScope) ->
            with(scope) { callWithContext(scoped) }
        }
        every { mock.call(eq5) } answers answer
        assertEquals(8, mock.call(eq5))
        verify { ctx(any<MokkeryCallScope>()) { scoped.callWithContext() } }
    }
}

context(scope: MokkeryCallScope)
private fun callWithContext(mock: ScopeMembers): Int = mock.callWithContext()

interface ArgMatcherMembers {
    val matcher: ArgMatcher<Int>
    fun call(matcher: ArgMatcher<Int>): Int
    fun callVararg(vararg matchers: ArgMatcher<Int>): Int
    fun make(): ArgMatcher<Int>
}

interface ScopeMembers {
    fun call(scope: MokkerySuiteScope): Int
    fun MokkeryScope.callWithReceiver(): Int
    context(scope: MokkeryCallScope)
    fun callWithContext(): Int
}

interface GenericMember<T> {
    fun call(value: T): Int
}
